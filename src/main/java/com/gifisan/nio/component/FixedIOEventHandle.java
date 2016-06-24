package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.server.service.FutureAcceptor;

public class FixedIOEventHandle extends IOEventHandleAdaptor {

	private ApplicationContext	applicationContext	= null;
	private FutureAcceptor		filterService		= null;
	private Logger				logger			= LoggerFactory.getLogger(FixedIOEventHandle.class);

	public FixedIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
		this.filterService = applicationContext.getFilterService();
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		try {
			filterService.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			exceptionCaughtOnWrite(session, future, null, e);
		}
	}

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) {
		DatagramPacketAcceptor acceptor = applicationContext.getDatagramPacketAcceptor();

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

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {

	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {

	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

}
