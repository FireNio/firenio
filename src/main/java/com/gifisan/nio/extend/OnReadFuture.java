package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;

public interface OnReadFuture {
	
	public static final EmptyOnReadFuture EMPTY_ON_READ_FUTURE = new EmptyOnReadFuture();
	
	public abstract void onResponse(Session session ,ReadFuture future);
}
