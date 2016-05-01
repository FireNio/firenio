package com.gifisan.nio.common;

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

}
