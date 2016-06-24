package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;

public class ReadFutureFactory {

	public static ReadFuture create(Session session, ReadFuture future) {

		return new TextReadFuture(session, 0, future.getServiceName());
	}

	public static ReadFuture create(Session session, String serviceName) {

		return new TextReadFuture(session, 0, serviceName);
	}

}
