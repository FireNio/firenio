package com.generallycloud.test.nio.common;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.codec.base.future.BaseReadFutureImpl;
import com.generallycloud.nio.codec.http11.future.ClientHttpReadFuture;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.Session;

public class ReadFutureFactory {

	public static BaseReadFuture create(Session session, BaseReadFuture future) {
		BaseReadFuture readFuture = (BaseReadFuture) future;
		return create(session, readFuture.getFutureID(), readFuture.getFutureName(), readFuture.getIOEventHandle());
	}

	public static BaseReadFuture create(Session session, Integer futureID, String serviceName,
			IOEventHandle ioEventHandle) {

		BaseReadFutureImpl textReadFuture = new BaseReadFutureImpl(session.getContext(),futureID, serviceName);

		textReadFuture.setIOEventHandle(ioEventHandle);

		return textReadFuture;
	}

	public static BaseReadFuture create(Session session, Integer futureID, String serviceName) {

		return create(session, futureID, serviceName, session.getContext().getIOEventHandleAdaptor());
	}

	public static BaseReadFuture create(Session session, String serviceName, IOEventHandle ioEventHandle) {

		return create(session, 0, serviceName, ioEventHandle);
	}

	public static HttpReadFuture createHttpReadFuture(Session session, String url) {
		return new ClientHttpReadFuture(session.getContext(),url, "GET");
	}

	public static BaseReadFuture create(Session session, String serviceName) {

		return create(session, 0, serviceName, session.getContext().getIOEventHandleAdaptor());
	}
}
