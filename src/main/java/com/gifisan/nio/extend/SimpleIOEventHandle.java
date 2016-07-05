package com.gifisan.nio.extend;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;

public class SimpleIOEventHandle extends IOEventHandleAdaptor {

	private Logger			logger		= LoggerFactory.getLogger(SimpleIOEventHandle.class);
	private FixedSession	fixedSession	;
	
	protected SimpleIOEventHandle(FixedSession fixedSession) {
		this.fixedSession = fixedSession;
	}
	
	public void acceptAlong(Session session, ReadFuture future){
		
		FixedSession fixedSession = this.fixedSession;
		
		try {
			
			fixedSession.accept(session, future);
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(), e);
			
			exceptionCaughtOnWrite(session, future, null, e);
		}
	}
	
	public void setContext(NIOContext context) {
		this.threadPool = context.getThreadPool();
		super.setContext(context);
	}

	public void futureSent(Session session, WriteFuture future) {
		
	}
	
//	private AtomicInteger sent = new AtomicInteger(1);
//	
//	public void futureSent(Session session, WriteFuture future) {
//		logger.info("sent:{}",sent.getAndIncrement());
//	}
	
	

}
