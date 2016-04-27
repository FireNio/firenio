package com.gifisan.nio.client;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public interface OnReadFuture {
	
	public static final EmptyOnReadFuture EMPTY_ON_READ_FUTURE = new EmptyOnReadFuture();
	
	public abstract void onResponse(Session sesssion ,ReadFuture future);
}
