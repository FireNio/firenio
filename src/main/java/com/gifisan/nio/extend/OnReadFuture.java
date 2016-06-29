package com.gifisan.nio.extend;

import com.gifisan.nio.component.future.EmptyOnReadFuture;
import com.gifisan.nio.component.future.ReadFuture;

public interface OnReadFuture {
	
	public static final EmptyOnReadFuture EMPTY_ON_READ_FUTURE = new EmptyOnReadFuture();
	
	public abstract void onResponse(FixedSession session ,ReadFuture future);
}
