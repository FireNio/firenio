package com.gifisan.nio.client;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;

public class ClientIOEventHandle extends IOEventHandleAdaptor {
	
	private Logger logger = LoggerFactory.getLogger(ClientIOEventHandle.class);

	public void sessionOpened(Session session) {

		logger.info("session opend,{}",session);
		
	}

	public void sessionClosed(Session session) {

		logger.info("session closed,{}",session);
	}

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		logger.info("exception,{}",cause);
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		logger.info("exception,{}",cause);
	}

	public void futureReceived(Session session, ReadFuture future) {
		logger.info("future received,{}",future);
	}

	public void futureSent(Session session, WriteFuture future) {
		logger.info("future sent,{}",future);
	}

}
