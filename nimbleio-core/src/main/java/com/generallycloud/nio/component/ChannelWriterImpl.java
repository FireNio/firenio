package com.generallycloud.nio.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.WriterOverflowException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.LinkedList;
import com.generallycloud.nio.component.concurrent.LinkedListABQ;
import com.generallycloud.nio.component.concurrent.ReentrantList;
import com.generallycloud.nio.component.protocol.IOWriteFuture;

//FIXME 问题好像出在这里
public class ChannelWriterImpl implements ChannelWriter {

	protected LinkedList<IOWriteFuture>		writerQueue;
	private Map<Integer, List<IOWriteFuture>>	sleepEndPoints	= new HashMap<Integer, List<IOWriteFuture>>();
	private Logger							logger		= LoggerFactory.getLogger(ChannelWriterImpl.class);
	private ReentrantList<ChannelWriteEvent>	events		= new ReentrantList<ChannelWriteEvent>();
	private NIOContext						context		= null;

	public ChannelWriterImpl(NIOContext context) {
		
		this.context = context;
		
		ServerConfiguration configuration = context.getServerConfiguration();

		int capacity = configuration.getSERVER_WRITE_QUEUE_SIZE();

		this.writerQueue = new LinkedListABQ<IOWriteFuture>(capacity);
	}

	public void fire(ChannelWriteEvent event) {
		events.add(event);
	}

	public void offer(IOWriteFuture future) {

		if (!this.writerQueue.offer(future)) {

			future.onException(WriterOverflowException.INSTANCE);
		}
	}

	public void wekeupEndPoint(TCPEndPoint endPoint) {

		Integer endPointID = endPoint.getEndPointID();

		List<IOWriteFuture> list = sleepEndPoints.get(endPointID);

		if (list == null) {
			return;
		}

		for (IOWriteFuture f : list) {

			this.offer(f);
		}

		sleepEndPoints.remove(endPointID);
	}

	private void sleepWriter(TCPEndPoint endPoint, IOWriteFuture future) {

		Integer endPointID = endPoint.getEndPointID();

		List<IOWriteFuture> list = sleepEndPoints.get(endPointID);

		if (list == null) {

			list = new ArrayList<IOWriteFuture>();

			sleepEndPoints.put(endPointID, list);
		}

		list.add(future);
	}

	private void fireEvents(List<ChannelWriteEvent> events) {

		for (ChannelWriteEvent e : events) {

			e.handle(this);
		}

		events.clear();
	}

	public void loop() {

		List<ChannelWriteEvent> events = this.events.getSnapshot();

		if (!events.isEmpty()) {

			fireEvents(events);
		}

		IOWriteFuture futureFromQueue = writerQueue.poll(16);

		if (futureFromQueue == null) {

			return;
		}

		TCPEndPoint endPoint = futureFromQueue.getEndPoint();

		if (!endPoint.isOpened()) {

			endPoint.decrementWriter();

			futureFromQueue.onException(new DisconnectException("disconnected"));

			return;
		}

		if (endPoint.isNetworkWeak()) {

			this.sleepWriter(endPoint, futureFromQueue);

			return;
		}

		try {

			IOWriteFuture futureFromEndPoint = endPoint.getCurrentWriteFuture();

			if (futureFromEndPoint != null) {

				doWriteFutureFromEndPoint(futureFromEndPoint, futureFromQueue, endPoint);

				return;
			}

			doWriteFutureFromWriters(futureFromQueue, endPoint);

		} catch (IOException e) {
			logger.debug(e);

			CloseUtil.close(endPoint);

			futureFromQueue.onException(e);

		} catch (Throwable e) {
			logger.debug(e);

			CloseUtil.close(endPoint);

			futureFromQueue.onException(new IOException(e));
		}
	}

	// write future from endPoint
	private void doWriteFutureFromEndPoint(IOWriteFuture futureFromEndPoint, IOWriteFuture futureFromQueue,
			TCPEndPoint endPoint) throws IOException {

		if (futureFromEndPoint.write()) {
			
			endPoint.decrementWriter();

			endPoint.setCurrentWriteFuture(null);

			futureFromEndPoint.onSuccess();

			doWriteFutureFromWriters(futureFromQueue, endPoint);

			return;
		}

		if (!writerQueue.offer(futureFromQueue)) {

			endPoint.decrementWriter();

			logger.debug("give up {}", futureFromQueue);

			futureFromEndPoint.onException(WriterOverflowException.INSTANCE);
		}
	}

	// write future from writers
	private void doWriteFutureFromWriters(IOWriteFuture futureFromQueue, TCPEndPoint endPoint) throws IOException {

		if (futureFromQueue.write()) {
			
			endPoint.decrementWriter();

			futureFromQueue.onSuccess();

		} else {

			endPoint.setCurrentWriteFuture(futureFromQueue);

			offer(futureFromQueue);
		}
	}

	public void stop() {

	}

	public String toString() {
		
		IOService service = context.getTCPService();
		
		return service.getServiceDescription() + "(Writer)";
	}

	public interface ChannelWriteEvent {

		void handle(ChannelWriter channelWriter);
	}
}
