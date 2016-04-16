package com.gifisan.nio.client.session;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.client.ClientConnection;
import com.gifisan.nio.client.ClientRequest;
import com.gifisan.nio.client.ClientRequestTask;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.StringUtil;

public class UniqueSession implements ClientSesssion {

	private ClientConnection		connection	= null;
	private long				timeout		= 0;
	private ClientRequest		request		= new ClientRequest();
	private ClientRequestTask	requestTask	= null;

	public UniqueSession(ClientConnection connection,ClientRequestTask requestTask) {
		this.connection = connection;
		this.requestTask = requestTask;
	}

	public long getTimeout() {
		return timeout;
	}

	public ClientResponse request(String serviceName, String text) throws IOException {
		return request(serviceName, text, null);
	}

	public ClientResponse request(String serviceName, String text, InputStream inputStream) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientConnection connection = this.connection;

		ClientRequest request = this.request;
		
		request.setServiceName(serviceName);
		
		request.setText(text);
		
		request.setInputStream(inputStream);
		
		this.requestTask.offer(request);

		return connection.acceptResponse(timeout);
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
