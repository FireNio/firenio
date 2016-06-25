package com.gifisan.nio.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.IOWriteFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ServerEndPointWriter implements EndPointWriter {

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

	public void loop() {

		if (collect) {

			collectEndPoints();
		}

		IOWriteFuture futureFromWriters = writers.poll(16);

		if (futureFromWriters == null) {

			return;
		}

		TCPEndPoint endPoint = futureFromWriters.getEndPoint();

		if (endPoint.isEndConnect()) {
			if (endPoint.isOpened()) {
				CloseUtil.close(endPoint);
			}
			endPoint.decrementWriter();
			futureFromWriters.onException(DisconnectException.INSTANCE);
			return;
		}

		if (endPoint.isNetworkWeak()) {

			this.lazyWriter(futureFromWriters);

			return;
		}

		try {

			IOWriteFuture futureFromEndPoint = endPoint.getCurrentWriter();

			if (futureFromEndPoint != null) {

				doWriteFutureFromEndPoint(futureFromEndPoint, futureFromWriters, endPoint);

				return;
			}

			doWriteFutureFromWriters(futureFromWriters, endPoint);

		} catch (IOException e) {
			logger.debug(e);

			CloseUtil.close(endPoint);

			futureFromWriters.onException(e);

		} catch (Exception e) {
			logger.debug(e);

			CloseUtil.close(endPoint);

			futureFromWriters.onException(new IOException(e));
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

			return;
		}

		if (!writers.offer(futureFromWriters)) {

			endPoint.decrementWriter();

			logger.debug("give up {}", futureFromWriters.getFutureID());

			futureFromEndPoint.onException(WriterOverflowException.INSTANCE);
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

			offer(futureFromWriters);
		}
	}
	
	public void stop() {
		
	}

	public String toString() {
		return "Server-EndPoint-Writer";
	}
}
