package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.mtp.common.StringUtil;

public class MultiSession implements ClientSesssion {

	private byte			sessionID		= 0;
	private ClientConnection	connection	= null;
	private long			timeout		= 0;
	private MessageBus		bus			= null;

	protected MultiSession(ClientConnection connection, MessageBus bus, byte sessionID) {
		this.connection = connection;
		this.sessionID = sessionID;
		this.bus = bus;
	}

	public long getTimeout() {
		return timeout;
	}

	public Response request(String serviceName, String content) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientConnection connection = this.connection;

		connection.write(this.sessionID, serviceName, content);

		MessageBus bus = this.bus;

		bus.await(timeout);

		return bus.getResponse();
	}

	public Response request(String serviceName, String content, InputStream inputStream) throws IOException {
		throw new IllegalStateException("can not trans stream when multi session");
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
