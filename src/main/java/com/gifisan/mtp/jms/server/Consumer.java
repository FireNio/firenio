package com.gifisan.mtp.jms.server;

import java.io.IOException;

import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class Consumer {
	
	public Consumer(Request request, Response response) {
		this.request = request;
		this.response = response;
	}

	private Request request = null;

	private Response response = null;

	public Response getResponse() {
		return response;
	}

	public void setResponse(Response response) {
		this.response = response;
	}
	
	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public void push(Message message) throws IOException{
		
		String content = message.toString();
		
		response.write(content);
		
		response.flush();
		
	}
	
}
