package com.gifisan.nio.component;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServiceAcceptor;

public abstract class AsynchServiceAcceptor implements Runnable, ServiceAcceptor {

	private Request	request	= null;
	private Response	response	= null;

	public AsynchServiceAcceptor(Request request, Response response) {
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
