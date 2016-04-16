package com.gifisan.nio.client;


public class TouchDistantJob implements Runnable {

	private ClientRequestTask	task	= null;
	
	private ClientRequest request = new ClientRequest();

	public TouchDistantJob(ClientRequestTask task) {
		this.task = task;
	}

	public void run() {
		task.offer(request);
	}
}
