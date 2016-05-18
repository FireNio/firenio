package com.gifisan.nio.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ServerEndPointWriter extends AbstractLifeCycle implements EndPointWriter {

	private Thread						owner		= null;
	private boolean					running		= false;
	private boolean					collect		= false;
	private LinkedList<IOWriteFuture>		writers		= new LinkedListM2O<IOWriteFuture>(1024 * 512);
	private Map<Long, List<IOWriteFuture>>	lazyEndPoints	= new HashMap<Long, List<IOWriteFuture>>();
	private Logger						logger		= LoggerFactory.getLogger(ServerEndPointWriter.class);

	public void collect() {
		this.collect = true;
	}

	public void offer(IOWriteFuture future) {

		if (!this.writers.offer(future)) {

			future.onException(WriterOverflowException.INSTANCE);
		}
	}

	private void collectEndPoints() {
		collect = false;

		Set<Entry<Long, List<IOWriteFuture>>> entries = lazyEndPoints.entrySet();

		Iterator<Entry<Long, List<IOWriteFuture>>> iterator = entries.iterator();

		for (; iterator.hasNext();) {
			Entry<Long, List<IOWriteFuture>> entry = iterator.next();

			List<IOWriteFuture> list = entry.getValue();

			if (list.size() == 0) {
				lazyEndPoints.remove(entry.getKey());
				continue;
			}

			IOWriteFuture future = list.get(0);

			TCPEndPoint endPoint = future.getEndPoint();

			if (endPoint.isNetworkWeak()) {
				continue;
			}

			for (IOWriteFuture _Future : list) {

				offer(_Future);
			}

			iterator.remove();

		}
	}

	private void lazyWriter(IOWriteFuture future) {
		Long endPointID = future.getEndPoint().getEndPointID();
		List<IOWriteFuture> list = lazyEndPoints.get(endPointID);

		if (list == null) {
			list = new ArrayList<IOWriteFuture>();
			lazyEndPoints.put(endPointID, list);
		}

		list.add(future);
	}

	public void run() {

		for (; running;) {

			if (collect) {

				collectEndPoints();
			}

			IOWriteFuture futureFromWriters = writers.poll(16);

			if (futureFromWriters == null) {

				continue;
			}

			TCPEndPoint endPoint = futureFromWriters.getEndPoint();

			if (endPoint.isEndConnect()) {
				if (endPoint.isOpened()) {
					CloseUtil.close(endPoint);
				}
				endPoint.decrementWriter();
				futureFromWriters.onException(DisconnectException.INSTANCE);
				continue;
			}

			if (endPoint.isNetworkWeak()) {

				this.lazyWriter(futureFromWriters);

				continue;
			}

			try {

				IOWriteFuture futureFromEndPoint = endPoint.getCurrentWriter();

				if (futureFromEndPoint != null) {

					doWriteFutureFromEndPoint(futureFromEndPoint, futureFromWriters, endPoint);

					continue;
				}

				doWriteFutureFromWriters(futureFromWriters, endPoint);

			} catch (IOException e) {
				DebugUtil.debug(e);

				CloseUtil.close(endPoint);

				futureFromWriters.onException(e);

			} catch (Exception e) {
				DebugUtil.debug(e);

				CloseUtil.close(endPoint);

				futureFromWriters.onException(new IOException(e));
			}
		}
	}

	// write future from endPoint
	private void doWriteFutureFromEndPoint(IOWriteFuture futureFromEndPoint, IOWriteFuture futureFromWriters,
			TCPEndPoint endPoint) throws IOException {

		if (futureFromEndPoint.write()) {

			endPoint.decrementWriter();

			endPoint.setCurrentWriter(null);

			futureFromEndPoint.onSuccess();

			doWriteFutureFromWriters(futureFromWriters, endPoint);

			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}

		} else {

			if (!writers.offer(futureFromWriters)) {

				endPoint.decrementWriter();

				logger.debug("give up {}", futureFromWriters.getFutureID());

				futureFromEndPoint.onException(WriterOverflowException.INSTANCE);
			}
		}
	}

	// write future from writers
	private void doWriteFutureFromWriters(IOWriteFuture futureFromWriters, TCPEndPoint endPoint) throws IOException {

		if (futureFromWriters.write()) {

			endPoint.decrementWriter();

			futureFromWriters.onSuccess();

			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}

		} else {

			endPoint.setCurrentWriter(futureFromWriters);
		}
	}

	protected void doStart() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "Server-EndPoint-Writer");
		this.owner.start();

	}

	protected void doStop() throws Exception {
		running = false;
	}
}
