package com.gifisan.nio.extend;

import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public interface OnReadFuture {
	
	public static final EmptyOnReadFuture EMPTY_ON_READ_FUTURE = new EmptyOnReadFuture();
	
	public abstract void onResponse(FixedSession session ,NIOReadFuture future);
}
