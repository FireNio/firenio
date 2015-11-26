package com.yoocent.mtp.component;

import java.io.IOException;

import com.yoocent.mtp.common.CloseUtil;
import com.yoocent.mtp.server.InnerEndPoint;
import com.yoocent.mtp.server.InnerRequest;
import com.yoocent.mtp.server.InnerResponse;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class ServletAcceptAbleJobImpl implements ServletAcceptAbleJob{
	
	private InnerRequest request = null;
	
	private InnerResponse response = null;
	
	private ServletService service = null;
	
	private InnerEndPoint endPoint = null;
	
	public ServletAcceptAbleJobImpl(ServletService service,InnerEndPoint endPoint
			,InnerRequest request,InnerResponse response) {
		this.service = service;
		this.endPoint = endPoint;
		this.request = request;
		this.response = response;
	}

	public void run() {
		try {
			this.doJob();
		} catch (ChannelException e){
			e.printStackTrace();
		} catch (IOException exception){
			exception.printStackTrace();
			//error connector
			this.acceptException(exception);
		}
	}
	
	private void acceptException(IOException exception){
		try {
			response.flush();
		} catch (IOException e) {
			//ignore
			e.printStackTrace();
		}
		//just close it
		CloseUtil.close(endPoint);
	}
	
	public void accept(Request request, Response response) throws IOException {
		service.accept(request, response);
	}

	public void doJob() throws IOException {
		this.accept(request, response);
	}

}
