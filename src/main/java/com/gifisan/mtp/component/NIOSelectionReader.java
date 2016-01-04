package com.gifisan.mtp.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.EndPoint;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.server.selector.SelectionAccept;

public class NIOSelectionReader implements SelectionAccept {

	private ServletContext		context				= null;
	private ThreadPool			servletDispatcher		= null;
	private ServletService		service				= null;
	private ExecutorThreadPool	asynchServletDispatcher	= null;

	public NIOSelectionReader(ServletContext context, ThreadPool servletDispatcher,
			ExecutorThreadPool asynchServletDispatcher, ServletService service) {
		this.context = context;
		this.servletDispatcher = servletDispatcher;
		this.asynchServletDispatcher = asynchServletDispatcher;
		this.service = service;
	}

	protected boolean isEndPoint(Object object) {
		if (object == null) {
			return false;
		}

		return object.getClass() == ServerNIOEndPoint.class || object instanceof EndPoint;

	}

	private ServerEndPoint getEndPoint(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.channel();

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {
			return (ServerEndPoint) attachment;
		}

		ServerEndPoint endPoint = new ServerNIOEndPoint(channel);

		selectionKey.attach(endPoint);

		return endPoint;

	}

	public void accept(SelectionKey selectionKey) throws Exception {

		ServletContext context = this.context;

		ServerEndPoint endPoint = getEndPoint(selectionKey);

		/*
		 * if (!selectionKey.isConnectable()) { endPoint.endConnect();
		 * SelectableChannel channel = selectionKey.channel();
		 * CloseUtil.close(channel); selectionKey.cancel(); return; }
		 */

		if (endPoint.inStream()) {
			synchronized (endPoint) {
				endPoint.notify();
				return;
			}
		}

		// ProtocolDecoder decoder = endPoint.getProtocolDecoder();

		boolean decoded = endPoint.protocolDecode(context);

		if (!decoded) {
			CloseUtil.close(endPoint);
			return;
		}

		ProtocolDecoder decoder = endPoint.getProtocolDecoder();

		if (decoder.isBeat()) {
			return;
		}

		// if (endPoint.isEndConnect()) {
		// CloseUtil.close(endPoint);
		// return;
		// }

		Request request = new MTPServletRequest(context, endPoint, asynchServletDispatcher);

		Response response = new MTPServletResponse(endPoint);

		ServletAcceptJob job = new ServletAcceptJobImpl(service, endPoint, request, response);

		/*
		 * 
		 * synchronized (endPoint) { if (endPoint.inSchedule()) {
		 * endPoint.schedule(job); }else{ endPoint.pushSchedule();
		 * threadPool.dispatch(job); } }
		 */
		servletDispatcher.dispatch(job);

	}

}
