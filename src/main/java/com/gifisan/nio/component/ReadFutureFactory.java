package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;
import com.gifisan.nio.server.ServerSession;

public class ReadFutureFactory {

	public static ReadFuture create(ReadFuture future) {

		IOReadFuture _Future = (IOReadFuture) future;

		return new TextReadFuture(
				_Future.getEndPoint(), 
				0, 
				_Future.getServiceName());

	}
	
	public static ReadFuture create(Session session,String serviceName) {

		ServerSession serverSession = (ServerSession) session;
		
		return new TextReadFuture(
				serverSession.getEndPoint(),
				0, 
				serviceName);

	}

}
