package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.OutputStream;

public class UniqueSession implements ClientSesssion {

	private ClientConnection	connection	= null;
	private long			timeout		= 0;

	protected UniqueSession(ClientConnection connection) {
		this.connection = connection;
	}

	public long getTimeout() {
		return timeout;
	}

	public Response request(String serviceName, String text) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientConnection connection = this.connection;

		connection.write((byte) 0, serviceName, text);

		return connection.acceptResponse();
	}

	public Response request(String serviceName, String text, int available) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		if (available == 0) {
			throw new IOException("empty service inputStream");
		}
		
		ClientConnection connection = this.connection;

		connection.write((byte) 0, serviceName, text, available);

		return connection.acceptResponse();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public OutputStream getOutputStream() {
		return connection.getOutputStream();
	}
	
	

}
