package com.gifisan.mtp.server;

public abstract class AsynchServletAcceptJob implements Runnable, ServletAccept {

	private Request	request	= null;
	private Response	response	= null;

	public AsynchServletAcceptJob(Request request, Response response) {
		this.request = request;
		this.response = response;
	}

	public void run() {
		try {
			accept(request, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
