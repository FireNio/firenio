package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public class NIOSelectionReader extends AbstractNIOSelection implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	= null;

	public NIOSelectionReader(NIOContext context) {
		super(context);
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		EndPoint endPoint = getEndPoint(context, selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {
			
			ProtocolDecoder decoder = context.getProtocolDecoder();

			future = decoder.decode(endPoint);

			if (future == null) {
				if (endPoint.isEndConnect()) {
					CloseUtil.close(endPoint);
				}
				return;
			}
			
			endPoint.setReadFuture(future);
		}

		if (future.read()) {
			
			endPoint.setReadFuture(null);

			readFutureAcceptor.accept(future.getSession(),future);
		}

//		if (endPoint.inStream()) {
//
//			if (endPoint.flushServerOutputStream(cache)) {
//
//				Session session = endPoint.getCurrentSession();
//
//				session.setStream(false);
//
//				endPoint.setCurrentSession(session);
//
//				ServiceAcceptorJob job = session.getServiceAcceptorJob(protocolData);
//
//				acceptorDispatcher.dispatch(job);
//				return;
//			}
//			return;
//		}
//
//		EndPointSchedule schedule = endPoint.getSchedule();
//
//		if (schedule != null) {
//			if (schedule.schedule(endPoint)) {
//				dispatch(endPoint, schedule.getProtocolData());
//			}
//			return;
//		}
//
//		ProtocolDecoder decoder = context.getProtocolDecoder();
//
//		ServerProtocolData protocolData = new ServerProtocolData();
//
//		boolean decoded = decoder.decode(endPoint, protocolData);
//
//		if (!decoded) {
//			if (endPoint.isEndConnect()) {
//				CloseUtil.close(endPoint);
//			}
//			return;
//		}
//
//		if (protocolData.isBeat()) {
//			return;
//		}
//
//		if (protocolData.getProtocolType() != ProtocolDecoder.TEXT) {
//
//			Session session = endPoint.getSession(protocolData.getSessionID());
//
//			session.setStream(true);
//
//			endPoint.setCurrentSession(session);
//
//			ServiceAcceptorJob job = session.getServiceAcceptorJob(protocolData);
//
//			job.run();
//
//			return;
//		}
//
//		dispatch(endPoint, protocolData);

	}

//	private void dispatch(EndPoint endPoint, ProtocolData protocolData) {
//
//		Session session = endPoint.getSession(protocolData.getSessionID());
//
//		ServiceAcceptorJob job = session.getServiceAcceptorJob(protocolData);
//
//		acceptorDispatcher.dispatch(job);
//	}

}
