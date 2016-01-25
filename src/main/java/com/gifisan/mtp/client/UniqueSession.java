package com.gifisan.mtp.client;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.mtp.common.StringUtil;

public class UniqueSession implements ClientSesssion {

	private ClientConnection	connection	= null;
	private long			timeout		= 0;

	protected UniqueSession(ClientConnection connection) {
		this.connection = connection;
	}

	public long getTimeout() {
		return timeout;
	}

	public Response request(String serviceName, String content) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientConnection connection = this.connection;

		connection.write((byte) 0, serviceName, content);

		return connection.acceptResponse();
	}

	public Response request(String serviceName, String content, InputStream inputStream) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientConnection connection = this.connection;

		connection.write((byte) 0, serviceName, content, inputStream);

		return connection.acceptResponse();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
