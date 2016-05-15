package com.gifisan.nio.client;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListO2O;
import com.gifisan.nio.concurrent.ThreadPool;

public class MessageBus {

	private LinkedList<ReadFuture>	futures		= new LinkedListO2O<ReadFuture>();
	private LinkedList<OnReadFuture>	onReadFutures	= new LinkedListO2O<OnReadFuture>(1024 * 10);
	private Map<String, OnReadFuture>	listeners		= new HashMap<String, OnReadFuture>();
	private ProtectedClientSession	clientSession	= null;
	private ThreadPool				executor		= null;
	
	protected MessageBus(ProtectedClientSession clientSession) {
		this.clientSession = clientSession;
		this.executor = clientSession.getContext().getExecutorThreadPool();
	}

	public ReadFuture poll(long timeout) throws DisconnectException {
		if (timeout == 0) {

			for (;;) {

				ReadFuture future = futures.poll(16);

				if (clientSession.closed()) {
					throw DisconnectException.INSTANCE;
				}
				
				if (future == null) {
					continue;
				}

				return future;
			}
		}

		ReadFuture future = futures.poll(timeout);
		
		if (future == null && clientSession.closed()) {
			throw DisconnectException.INSTANCE;
		}

		return future;
	}

	public void filterOffer(final ReadFuture future) {

		final OnReadFuture onReadFuture = listeners.get(future.getServiceName());

		if (onReadFuture != null) {
			
			this.executor.dispatch(new Runnable() {
				
				public void run() {
					onReadFuture.onResponse((ProtectedClientSession) ((IOReadFuture) future).getSession(), future);
				}
			});
			
			return;
		}

		final OnReadFuture onReadFuture1 = this.onReadFutures.poll();

		if (onReadFuture1 != null) {
		
			executor.dispatch(new Runnable() {
				
				public void run() {
					ProtectedClientSession session = (ProtectedClientSession) ((IOReadFuture) future).getSession();
					try {
						onReadFuture1.onResponse(session, future);
					} catch (Exception e) {
						e.printStackTrace();
					}
					session.offer();
				}
			});
			return;
		}

		this.futures.offer(future);
	}
	
	public void offer(ReadFuture future){
		
		this.futures.offer(future);
	}

	public void listen(String serviceName, OnReadFuture onReadFuture) {
		this.listeners.put(serviceName, onReadFuture);
	}

	public void cancelListen(String serviceName) {
		this.listeners.remove(serviceName);
	}

	public void onReadFuture(OnReadFuture onReadFuture) {
		this.onReadFutures.forceOffer(onReadFuture);
	}
	
	public int size(){
		return futures.size();
	}
}
