package com.gifisan.mtp.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.EndPoint;
import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.InnerRequest;
import com.gifisan.mtp.server.InnerResponse;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class NIOSelectionReader implements SelectionAccept{

	private ServletContext context = null;
	private ThreadPool threadPool  = null;
	private ServletService service = null;
	
	public NIOSelectionReader(ServletContext context, ThreadPool threadPool,
			ServletService service) {
		this.context = context;
		this.threadPool = threadPool;
		this.service = service;
	}

	protected boolean isEndPoint(Object object){
		if (object == null) {
			return false;
		}
		
		return object.getClass() == NIOConnectorEndPoint.class || object instanceof EndPoint;
		
	}
	
	private InnerEndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		SocketChannel client = (SocketChannel) selectionKey.channel();
		
		Object attachment = selectionKey.attachment();
		
		if (isEndPoint(attachment)) {
			return (InnerEndPoint) attachment;
		}
		
		InnerEndPoint endPoint = new NIOConnectorEndPoint(selectionKey,client);
		
		selectionKey.attach(endPoint);
		
		return endPoint;
		
	}
	
	
	
	public void accept(SelectionKey selectionKey) throws Exception {
		
		InnerEndPoint endPoint = getEndPoint(selectionKey);
		

		/*
		if (!selectionKey.isConnectable()) {
			endPoint.endConnect();
			SelectableChannel channel = selectionKey.channel();
			CloseUtil.close(channel);
			selectionKey.cancel();
			return;
		}
		
		if (endPoint.inStream()) {
			synchronized (endPoint) {

				endPoint.notify();
				return;
			}
		}
		*/

		MTPParser parser = endPoint.genParser();
		
		parser.parse(context);
		if (!parser.isParseComplete()) {
				CloseUtil.close(endPoint);
			return;
		}
		
		if (endPoint.isEndConnect()) {
			CloseUtil.close(endPoint);
			return;
		}
		
		InnerRequest request = new MTPServletRequest(context,endPoint);
		
		InnerResponse response = new MTPServletResponse(endPoint);
		
		ServletAcceptJob job = new ServletAcceptJobImpl(service,endPoint, request, response);

		/*

		synchronized (endPoint) {
			if (endPoint.inSchedule()) {
				endPoint.schedule(job);
			}else{
				endPoint.pushSchedule();
				threadPool.dispatch(job);
			}
		}

		*/
		threadPool.dispatch(job);
		
	}

}
