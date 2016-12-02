package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public abstract class MQServlet extends ProtobaseFutureAcceptorService {

	private MQContext	context	= MQContext.getInstance();

	public MQContext getMQContext() {
		return context;
	}

	public void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {

		MQSessionAttachment attachment = context.getSessionAttachment(session);

		this.doAccept(session, future, attachment);
	}

	public abstract void doAccept(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment)
			throws Exception;

}
