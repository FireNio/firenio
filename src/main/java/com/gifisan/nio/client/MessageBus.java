package com.gifisan.nio.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageBus {

	private ClientResponse					response	= null;
	private ArrayBlockingQueue<ClientResponse>	queue	= new ArrayBlockingQueue<ClientResponse>(1);

	public void await(long timeout) {
		if (timeout == 0) {
			try {
				ClientResponse response = null;
				for (; response == null;) {
					response = queue.poll(16, TimeUnit.MILLISECONDS);

				}
				this.response = response;
			} catch (InterruptedException e) {
				e.printStackTrace();

			}
		} else {
			try {
				response = queue.poll(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public ClientResponse getResponse() {
		return response;
	}

	public void setResponse(ClientResponse response) {
		this.queue.offer(response);
	}

}
