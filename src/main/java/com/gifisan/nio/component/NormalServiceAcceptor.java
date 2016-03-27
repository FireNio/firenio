package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;

public class NormalServiceAcceptor implements ServiceAcceptorJob {

	private Logger				logger		= LoggerFactory.getLogger(NormalServiceAcceptor.class);
	private ServiceRequest		request		= null;
	private ServiceResponse		response		= null;
	private FilterService		service		= null;
	private ServerEndPoint		endPoint		= null;
	private ProtocolData		protocolData	= null;

	public NormalServiceAcceptor(ServerEndPoint endPoint, FilterService service, ServiceRequest request,
			ServiceResponse response) {
		this.endPoint = endPoint;
		this.service = service;
		this.request = request;
		this.response = response;
	}

	public void accept(Throwable exception) {
		try {
			// error connection , should not flush
			response.flush();
		} catch (IOException e) {
			// ignore
			logger.error(e.getMessage(), e);
			// just close it
			CloseUtil.close(endPoint);
		}
	}

	public void run() {
		try {
			this.accept(request, response);
		} catch (NIOException e) {
			logger.error(e.getMessage(), e);
		} catch (Throwable throwable) {
			logger.error(throwable.getMessage(), throwable);
			this.accept(throwable);
		} finally {
			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}
		}
	}

	public void accept(Request request, Response response) throws IOException {

		this.request.update(endPoint, protocolData);
		
		this.response.update();
		
		this.service.accept(request, response);
	}

	public ServiceAcceptorJob update(ServerEndPoint endPoint, ProtocolData protocolData) {
		this.protocolData = protocolData;
		this.endPoint = endPoint;
		return this;
	}
}
