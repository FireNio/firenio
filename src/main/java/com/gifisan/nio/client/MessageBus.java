package com.gifisan.nio.client;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageBus {

	private Response					response	= null;
	private ArrayBlockingQueue<Response>	queue	= new ArrayBlockingQueue<Response>(1);

	public void await(long timeout) {
		if (timeout == 0) {
			try {
				Response response = null;
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

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.queue.offer(response);
	}

}
