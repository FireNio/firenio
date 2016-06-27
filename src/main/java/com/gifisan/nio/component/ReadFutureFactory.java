package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;

public class ReadFutureFactory {
	
	public static ReadFuture create(Session session, ReadFuture future) {

		return create(session, future.getFutureID(), future.getServiceName());
	}

	public static ReadFuture create(Session session,Integer futureID, String serviceName) {

		return new TextReadFuture(session, futureID, serviceName);
	}
	
	public static ReadFuture create(Session session,String serviceName) {

		return create(session, 0, serviceName);
	}
}
