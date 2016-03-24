package com.gifisan.nio.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.common.StringUtil;

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

	public Response request(String serviceName, String text, InputStream inputStream) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		if (inputStream == null || inputStream.available() == 0) {
			return this.request(serviceName, text);
		}
		
		ClientConnection connection = this.connection;

		connection.write((byte) 0, serviceName, text, inputStream);

		return connection.acceptResponse();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}


}
