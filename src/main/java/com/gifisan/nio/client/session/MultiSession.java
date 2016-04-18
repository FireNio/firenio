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
		return request(serviceName, content, null);
	}

	public ClientResponse request(String serviceName, String content, InputStream inputStream) throws IOException {
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IOException("empty service name");
		}
		
		ClientRequest request = this.request;
		
		request.setServiceName(serviceName);
		
		request.setText(content);
		
		request.setInputStream(inputStream);
		
		requestTask.offer(request);

		MessageBus bus = this.bus;

		//FIXME 处理bus cancel后，收到消息
		bus.await(timeout);

		return bus.getResponse();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
