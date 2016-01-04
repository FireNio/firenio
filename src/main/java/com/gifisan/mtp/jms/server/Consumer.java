package com.gifisan.mtp.jms.server;

import java.io.IOException;

import com.gifisan.mtp.jms.JMSException;
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
	
	private TransactionSection getTransactionSection(Request request){
		
		return (TransactionSection) request.getSession().getAttribute("_MQ_TRANSACTION");
	}
	
	public void push(Message message) throws IOException{
		Request request = this.request;
		Response response = this.response;
		
		TransactionSection section = getTransactionSection(request);
		
		if (section != null) {
			section.offerMessage(message);
		}
		
		String content = message.toString();
		
		response.write(content);
		
		try {
			response.flush();
		} catch (Exception e) {
			e.printStackTrace();
			if (section!=null) {
				section.rollback();
			}
		}
		
	}
	
}
