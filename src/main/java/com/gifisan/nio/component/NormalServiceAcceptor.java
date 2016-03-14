package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.selector.ServiceAcceptor;

public class NormalServiceAcceptor implements ServiceAcceptor {

	private Logger				logger	= LoggerFactory.getLogger(NormalServiceAcceptor.class);
	private NIOServletRequest	request	= null;
	private NIOServletResponse	response	= null;
	private FilterService		service	= null;
	private ServerEndPoint		endPoint	= null;

	public NormalServiceAcceptor(ServerEndPoint endPoint, FilterService service, NIOServletRequest request,
			NIOServletResponse response) {
		this.endPoint = endPoint;
		this.service = service;
		this.request = request;
		this.response = response;
	}

	public void acceptException(Throwable exception) {
		try {
			// error connection , should not flush
			response.flush();
		} catch (IOException e) {
			// ignore
			logger.error(e.getMessage(),e);
			// just close it
			CloseUtil.close(endPoint);
		}
	}

	public void run() {
		try {
			this.accept(request, response);
		} catch (NIOException e) {
			logger.error(e.getMessage(),e);
		} catch(Throwable throwable){
			logger.error(throwable.getMessage(),throwable);
			this.acceptException(throwable);
		} finally {
			if (endPoint.isEndConnect()) {
				CloseUtil.close(endPoint);
			}
		}
	}

	public void accept(Request request, Response response) throws IOException {
		service.accept(request, response);
	}

	public ServiceAcceptor update(ServerEndPoint endPoint) {
		this.endPoint = endPoint;
		this.request.update(endPoint);
		this.response.update();
		return this;
	}
}
