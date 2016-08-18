package com.gifisan.nio.extend.plugin.http.client;

import java.io.IOException;

import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpRequestFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.WaiterOnReadFuture;

public class HttpClient {
	
	protected HttpClient(TCPConnector connector) {
		this.connector = connector;
	}
	
	private TCPConnector connector;

	private WaiterOnReadFuture listener = null;
	
	public HttpReadFuture request(Session session, HttpRequestFuture	 future, long timeout) throws IOException {

		this.listener = new WaiterOnReadFuture();

		session.flush(future);

		// FIXME 连接丢失时叫醒我
		if (!listener.await(timeout)) {

			return (HttpReadFuture) listener.getReadFuture();
		}

		CloseUtil.close(connector);
		
		throw new TimeoutException("timeout");

	}

	public WaiterOnReadFuture getListener(){
		return listener;
	}

}
