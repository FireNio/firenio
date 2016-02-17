package com.gifisan.mtp.server;

import com.gifisan.mtp.common.DebugUtil;

public abstract class AsynchServletAcceptJob implements Runnable, ServletAcceptor {

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
			DebugUtil.debug(e);
		}
	}

}
