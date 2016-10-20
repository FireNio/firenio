package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class HttpClient {

	protected HttpClient(SocketChannelConnector connector) {
		this.connector = connector;
	}

	private SocketChannelConnector	connector;

	private WaiterOnReadFuture		listener	= null;

	public HttpReadFuture request(Session session, HttpReadFuture future, long timeout) throws IOException {

		this.listener = new WaiterOnReadFuture();

		session.flush(future);

		// FIXME 连接丢失时叫醒我
		if (!listener.await(timeout)) {

			return (HttpReadFuture) listener.getReadFuture();
		}

		CloseUtil.close(connector);

		throw new TimeoutException("timeout");

	}

	public WaiterOnReadFuture getListener() {
		return listener;
	}

}
