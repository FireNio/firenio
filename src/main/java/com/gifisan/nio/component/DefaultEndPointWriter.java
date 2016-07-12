package com.gifisan.nio.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.WriterOverflowException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListM2O;
import com.gifisan.nio.component.concurrent.ReentrantList;
import com.gifisan.nio.component.protocol.future.IOWriteFuture;

//FIXME 问题好像出在这里
public class DefaultEndPointWriter implements EndPointWriter {

	protected LinkedList<IOWriteFuture>		writerQueue;
	private Map<Integer, List<IOWriteFuture>>	sleepEndPoints	= new HashMap<Integer, List<IOWriteFuture>>();
	private Logger							logger		= LoggerFactory.getLogger(DefaultEndPointWriter.class);
	private ReentrantList<EndPointWriteEvent>	events		= new ReentrantList<DefaultEndPointWriter.EndPointWriteEvent>();

	
	public DefaultEndPointWriter(int capacity) {
		this.writerQueue = new LinkedListM2O<IOWriteFuture>(capacity);
	}

	public void fire(EndPointWriteEvent event) {
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

		for(IOWriteFuture f :list){
			
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
	
	private void fireEvents(List<EndPointWriteEvent> events){
		
		for(EndPointWriteEvent e : events){
			
			e.handle(this);
		}
		
		events.clear();
	}

	public void loop() {

		List<EndPointWriteEvent> events = this.events.getSnapshot();
		
		if (!events.isEmpty()) {

			fireEvents(events);
		}

		IOWriteFuture futureFromWriters = writerQueue.poll(16);

		if (futureFromWriters == null) {

			return;
		}

		TCPEndPoint endPoint = futureFromWriters.getEndPoint();

		if (endPoint.isEndConnect()) {
			
			if (endPoint.isOpened()) {
			
				CloseUtil.close(endPoint);
			}
			
			endPoint.decrementWriter();
			
			futureFromWriters.onException(new DisconnectException("disconnected"));
			
			return;
		}

		if (endPoint.isNetworkWeak()) {

			this.sleepWriter(endPoint,futureFromWriters);

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

		if (!writerQueue.offer(futureFromWriters)) {

			endPoint.decrementWriter();

			logger.debug("give up {}", futureFromWriters);

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

	public interface EndPointWriteEvent {

		void handle(EndPointWriter endPointWriter);
	}
}
