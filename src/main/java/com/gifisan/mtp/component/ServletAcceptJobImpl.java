package com.gifisan.mtp.component;

import java.io.IOException;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class ServletAcceptJobImpl implements ServletAcceptJob{
	
	private Request request   = null;
	private Response response = null;
	private ServletService service = null;
	private ServerEndPoint endPoint = null;
	
	public ServletAcceptJobImpl(ServletService service,ServerEndPoint endPoint
			,Request request,Response response) {
		this.service = service;
		this.endPoint = endPoint;
		this.request = request;
		this.response = response;
	}

	private void acceptException(IOException exception){
		try {
			// error connection , should not flush
			response.flush();
		} catch (IOException e) {
			//ignore
			e.printStackTrace();
		}
		//just close it
		CloseUtil.close(endPoint);
	}
	
	
	public void schedule() {
		try {
			this.accept(request, response);
		} catch (MTPChannelException e){
			e.printStackTrace();
		} catch (IOException exception){
			exception.printStackTrace();
			//error connector
			this.acceptException(exception);
		}
		
	}

	public void accept(Request request, Response response) throws IOException {
		service.accept(request, response);
	}

}
