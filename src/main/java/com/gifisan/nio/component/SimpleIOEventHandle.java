package com.gifisan.nio.component;

import com.gifisan.nio.client.FixedIOSession;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.NIOContext;

public class SimpleIOEventHandle extends IOEventHandleAdaptor {

	private Logger			logger		= LoggerFactory.getLogger(SimpleIOEventHandle.class);

	private FixedSession	fixedSession	= null;
	
	private IOConnector		connector		= null;

	protected SimpleIOEventHandle(IOConnector connector) {
		this.connector = connector;
	}
	
	public FixedSession getFixedSession(){
		if (fixedSession == null) {
			
			Session session = connector.getSession();
			
			if (session == null) {
				return null;
			}
			
			fixedSession = new FixedIOSession(session);
		}
		return fixedSession;
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		FixedSession fixedSession = getFixedSession();
		
		try {
			fixedSession.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionCaughtOnWrite(session, future, null, e);
		}
	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) {

		Session session = endPoint.getSession();

		NIOContext context = session.getContext();

		DatagramPacketAcceptor acceptor = context.getDatagramPacketAcceptor();

		if (acceptor == null) {
			logger.debug("___________________null acceptor,packet:{}", packet);
			return;
		}

		// logger.debug("___________________client receive,packet:{}",packet);

		try {
			acceptor.accept(endPoint, packet);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

}
