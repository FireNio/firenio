package com.gifisan.nio.component;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;
import com.gifisan.nio.server.service.FutureAcceptor;


public class FixedIOEventHandle implements IOEventHandle{
	
	private Logger logger = LoggerFactory.getLogger(FixedIOEventHandle.class);
	
	protected FixedIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private ApplicationContext applicationContext = null;
	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	private FutureAcceptor filterService = null;

	public void sessionOpened(Session session) {
		
		
	}

	public void sessionClosed(Session session) {
		// TODO Auto-generated method stub
		
	}

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		// TODO Auto-generated method stub
		
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		// TODO Auto-generated method stub
		
	}

	public void futureReceived(Session session, ReadFuture future) {
		try {
			filterService.accept(session, future);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			exceptionCaughtOnWrite(session, future, null, e);
		}
	}

	public void futureSent(Session session, WriteFuture future) {
		// TODO Auto-generated method stub
		
	}
	
}
