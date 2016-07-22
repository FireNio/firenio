package com.gifisan.nio.extend.plugin.http.client;

import java.io.IOException;

import com.gifisan.nio.TimeoutException;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpRequestFuture;
import com.gifisan.nio.extend.OnReadFutureWrapper;
import com.gifisan.nio.extend.WaiterOnReadFuture;

public class HttpClient {
	
	protected HttpClient() {
	}

	private OnReadFutureWrapper listener = null;
	
	public HttpReadFuture request(Session session, HttpRequestFuture	 future, long timeout) throws IOException {

		WaiterOnReadFuture onReadFuture = new WaiterOnReadFuture();

		waiterListen(onReadFuture);

		session.flush(future);

		// FIXME 连接丢失时叫醒我
		if (onReadFuture.await(timeout)) {

			return (HttpReadFuture) onReadFuture.getReadFuture();
		}

		throw new TimeoutException("timeout");

	}

	private void waiterListen(WaiterOnReadFuture onReadFuture) throws IOException {

		if (onReadFuture == null) {
			throw new IOException("empty onReadFuture");
		}

		this.listener = new OnReadFutureWrapper();

		this.listener.listen(onReadFuture);
	}
	
	public OnReadFutureWrapper getListener(){
		return listener;
	}

}
