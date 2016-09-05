package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.WriteFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class SimpleIOEventHandle extends IOEventHandleAdaptor {

	private Logger			logger		= LoggerFactory.getLogger(SimpleIOEventHandle.class);
	private FixedSession	fixedSession	= new FixedIOSession();

	public void accept(Session session, ReadFuture future) {

		FixedSession fixedSession = this.fixedSession;

		try {

			fixedSession.accept(session, (NIOReadFuture) future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			exceptionCaught(session, future, e, IOEventState.HANDLE);
		}
	}

	protected void doStart() throws Exception {
		getContext().addSessionEventListener(new UpdateFixedSessionSEListener(fixedSession));
		super.doStart();
	}

	public void futureSent(Session session, WriteFuture future) {

	}

	public FixedSession getFixedSession() {
		return fixedSession;
	}

}
