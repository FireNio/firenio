package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;
import com.gifisan.nio.server.service.FilterService;


public class FixedIOEventHandle implements IOEventHandle{
	
	private ApplicationContext applicationContext = null;
	
	private FilterService filterService = null;

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
		filterService.accept(session, future);
		
	}

	public void futureSent(Session session, WriteFuture future) {
		// TODO Auto-generated method stub
		
	}
	
}
