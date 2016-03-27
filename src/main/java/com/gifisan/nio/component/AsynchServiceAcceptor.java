package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServiceAcceptor;

public abstract class AsynchServiceAcceptor implements Runnable, ServiceAcceptor {

	private Logger			logger	= LoggerFactory.getLogger(AsynchServiceAcceptor.class);
	private Request		request	= null;
	private InnerResponse	response	= null;

	public AsynchServiceAcceptor(Request request, Response response) {
		this.request = request;
		this.response = (InnerResponse) response;

	}

	public void accept(Throwable exception) {
		try {
			// error connection , should not flush
			response.flush();
		} catch (IOException e) {
			// ignore
			logger.error(e.getMessage(), e);
		}
	}

	public void run() {
		try {
			this.accept(request, response);
		} catch (NIOException e) {
			logger.error(e.getMessage(), e);
		} catch (Throwable throwable) {
			logger.error(throwable.getMessage(), throwable);
			this.accept(throwable);
		}
	}

}
