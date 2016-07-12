package com.gifisan.nio.component;

import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.component.protocol.nio.future.TextReadFuture;

public class ReadFutureFactory {

	public static NIOReadFuture create(Session session, NIOReadFuture future) {
		NIOReadFuture readFuture = (NIOReadFuture) future;
		return create(session, readFuture.getFutureID(), readFuture.getServiceName(), readFuture.getIOEventHandle());
	}

	public static NIOReadFuture create(Session session, Integer futureID, String serviceName,
			IOEventHandle ioEventHandle) {

		TextReadFuture textReadFuture = new TextReadFuture(session, futureID, serviceName);

		textReadFuture.setIOEventHandle(ioEventHandle);

		return textReadFuture;
	}
	
	public static NIOReadFuture create(Session session, Integer futureID, String serviceName) {

		return create(session, futureID, serviceName, session.getContext().getIOEventHandleAdaptor());
	}

	public static NIOReadFuture create(Session session, String serviceName, IOEventHandle ioEventHandle) {

		return create(session, 0, serviceName, ioEventHandle);
	}
	
	public static NIOReadFuture create(Session session, String serviceName) {

		return create(session, 0, serviceName, session.getContext().getIOEventHandleAdaptor());
	}
}
