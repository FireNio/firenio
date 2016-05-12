package com.gifisan.nio.server;

import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;

public class ReadFutureFactory {

	public static ServerReadFuture create(ReadFuture future) {

		IOReadFuture _Future = (IOReadFuture) future;

		return new TextReadFuture(
				_Future.getEndPoint(), 
				null, 
				_Future.getSession(), 
				_Future.getServiceName());

	}
	
	public static ServerReadFuture create(IOSession session,String serviceName) {

		ServerSession serverSession = (ServerSession) session;
		
		return new TextReadFuture(
				serverSession.getEndPoint(),
				null, 
				session, 
				serviceName);

	}

}
