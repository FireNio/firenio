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
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListABQ;

public class EndPointWriter extends AbstractLifeCycle implements Runnable {

	private Thread						owner			= null;
	private boolean					running			= false;
	private LinkedList<IOWriteFuture>		writers			= new LinkedListABQ<IOWriteFuture>(1024 * 80);
	private IOException					ioClosedException	= new IOException("disconnected");
	private boolean					collect			= false;
	private Map<Long, List<IOWriteFuture>>	lazyEndPoints		= new HashMap<Long, List<IOWriteFuture>>();

	public void collect() {
		this.collect = true;
	}

	public void forceOffer(IOWriteFuture writer) {

		this.writers.forceOffer(writer);
	}

	public boolean offer(IOWriteFuture writer) {

		return this.writers.offer(writer);
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

			EndPoint endPoint = future.getEndPoint();

			if (endPoint.isNetworkWeak()) {
				continue;
			}

			for (IOWriteFuture _Future : list) {

				offer0(_Future);
			}

			iterator.remove();

		}
	}

	private void offer0(IOWriteFuture future) {
		if (!this.writers.offer(future)) {

			future.catchException(WriterOverflowException.INSTANCE);
		}
	}

	private void trashWriter(IOWriteFuture future) {
		Long endPointID = future.getEndPoint().getEndPointID();
		List<IOWriteFuture> list = lazyEndPoints.get(endPointID);

		if (list == null) {
			list = new ArrayList<IOWriteFuture>();
			lazyEndPoints.put(endPointID, list);
		}

		list.add(future);
	}

	public void run() {

		byte unwriting = 0;

		for (; running;) {

			if (collect) {

				collectEndPoints();
			}

			IOWriteFuture writer = writers.poll(16);

			if (writer == null) {

				continue;
			}

			EndPoint endPoint = writer.getEndPoint();

			if (endPoint.isEndConnect()) {
				writer.catchException(ioClosedException);
				continue;
			}

			try {

				if (endPoint.isNetworkWeak()) {

					this.trashWriter(writer);

					continue;
				}

				if (!endPoint.enableWriting(writer.getFutureID())) {
					offer0(writer);
					continue;
				}

				if (writer.write()) {

					endPoint.setWriting(unwriting);

				} else {

					if (writer.isNetworkWeak()) {

						endPoint.setWriting(writer.getFutureID());

						endPoint.setCurrentWriter(writer);

						continue;
					}

					endPoint.setWriting(writer.getFutureID());

					if (!writers.offer(writer)) {
						CloseUtil.close(endPoint);

						writer.catchException(WriterOverflowException.INSTANCE);
					}

				}
			} catch (WriterOverflowException e) {
				DebugUtil.debug(e);

				writer.catchException(new IOException(e));

			} catch (IOException e) {
				DebugUtil.debug(e);

				CloseUtil.close(endPoint);

				writer.catchException(e);

			} catch (Exception e) {
				DebugUtil.debug(e);

				CloseUtil.close(endPoint);

				writer.catchException(new IOException(e));
			}
		}
	}

	public void doStart() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "EndPoint-Writer");
		this.owner.start();

	}

	public void doStop() throws Exception {
		running = false;
	}
}
