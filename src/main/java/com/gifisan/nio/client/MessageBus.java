package com.gifisan.nio.client;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListO2O;

public class MessageBus {

	private LinkedList<ReadFuture>	futures		= new LinkedListO2O<ReadFuture>();
	private LinkedList<OnReadFuture>	onReadFutures	= new LinkedListO2O<OnReadFuture>(1024 * 10);
	private Map<String, OnReadFuture>	listeners		= new HashMap<String, OnReadFuture>();

	public ReadFuture poll(long timeout) {
		if (timeout == 0) {

			for (;;) {

				ReadFuture future = futures.poll(16);

				if (future == null) {
					continue;
				}

				return future;
			}
		}

		ReadFuture future = futures.poll(timeout);

		return future;
	}

	public void filterOffer(ReadFuture future) {

		OnReadFuture onReadFuture = listeners.get(future.getServiceName());

		if (onReadFuture != null) {
			onReadFuture.onResponse((ProtectedClientSession) ((IOReadFuture) future).getSession(), future);
			return;
		}

		onReadFuture = this.onReadFutures.poll();

		if (onReadFuture != null) {
			ProtectedClientSession session = (ProtectedClientSession) ((IOReadFuture) future).getSession();
			try {
				onReadFuture.onResponse(session, future);
			} catch (Exception e) {
				e.printStackTrace();
			}
			session.offer();
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

}
