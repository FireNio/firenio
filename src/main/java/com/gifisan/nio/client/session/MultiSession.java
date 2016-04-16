package com.gifisan.nio.client.session;

import java.io.IOException;
import java.io.InputStream;

import com.gifisan.nio.client.ClientRequest;
import com.gifisan.nio.client.ClientRequestTask;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.MessageBus;
import com.gifisan.nio.common.StringUtil;

public class MultiSession implements ClientSesssion {

	private long				timeout		= 0;
	private MessageBus			bus			= null;
	private ClientRequestTask	requestTask	= null;
	private ClientRequest 		request 		= new ClientRequest();

	protected MultiSession(ClientRequestTask requestTask, MessageBus bus, byte sessionID) {
		this.requestTask = requestTask;
		this.request.setSessionID(sessionID);
		this.bus = bus;
	}

	public long getTimeout() {
		return timeout;
	}

	public ClientResponse request(String serviceName, String content) throws IOException {
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}

		ClientRequest request = this.request;
		
		request.setServiceName(serviceName);
		
		request.setText(content);
		
		requestTask.offer(request);

		MessageBus bus = this.bus;

		bus.await(timeout);

		return bus.getResponse();
	}

	public ClientResponse request(String serviceName, String content, InputStream inputStream) throws IOException {
		throw new IllegalStateException("can not trans stream when multi session");
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
