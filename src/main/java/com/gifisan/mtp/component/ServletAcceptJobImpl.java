package com.gifisan.mtp.component;

import java.io.IOException;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.InnerRequest;
import com.gifisan.mtp.server.InnerResponse;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class ServletAcceptJobImpl implements ServletAcceptJob{
	
	private InnerRequest request   = null;
	private InnerResponse response = null;
	private ServletService service = null;
	private InnerEndPoint endPoint = null;
	
	public ServletAcceptJobImpl(ServletService service,InnerEndPoint endPoint
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
