package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.OutputStream;

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

	public Response request(String serviceName, String content, int available) throws IOException {
		throw new IllegalStateException("can not trans stream when multi session");
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public OutputStream getOutputStream() {
		return connection.getOutputStream();
	}
	
	

}
