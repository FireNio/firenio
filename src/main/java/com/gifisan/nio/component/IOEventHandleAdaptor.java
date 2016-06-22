package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;

public class IOEventHandleAdaptor implements IOEventHandle {

	public void sessionOpened(Session session) {
		
	}

	public void sessionClosed(Session session) {
	}

	public void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause) {
		
	}

	public void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture, Exception cause) {
		
	}

	public void futureReceived(Session session, ReadFuture future) {
		
	}

	public void futureSent(Session session, WriteFuture future) {
		
	}


}
