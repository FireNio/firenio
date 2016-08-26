package com.gifisan.nio.extend;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.WriteFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class SimpleIOEventHandle extends IOEventHandleAdaptor {

	private Logger			logger		= LoggerFactory.getLogger(SimpleIOEventHandle.class);
	private FixedSession	fixedSession	= new FixedIOSession();

	public void acceptAlong(Session session, ReadFuture future) {

		FixedSession fixedSession = this.fixedSession;

		try {

			fixedSession.accept(session, (NIOReadFuture) future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			exceptionCaught(session, future, e, IOEventState.HANDLE);
		}
	}

	public void setContext(NIOContext context) {
		context.addSessionEventListener(new UpdateFixedSessionSEListener(fixedSession));
		super.setContext(context);
	}

	public void futureSent(Session session, WriteFuture future) {

	}

	public FixedSession getFixedSession() {
		return fixedSession;
	}

	// private AtomicInteger sent = new AtomicInteger(1);
	//
	// public void futureSent(Session session, WriteFuture future) {
	// logger.info("sent:{}",sent.getAndIncrement());
	// }

}
