package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.TextReadFuture;

public class ReadFutureFactory {

	public static ReadFuture create(ReadFuture future) {

		return create(future.getServiceName());
	}

	public static ReadFuture create(String serviceName) {

		return new TextReadFuture(serviceName);
	}

}
