package com.gifisan.nio.client;

import com.gifisan.nio.component.ReadFuture;

public interface OnReadFuture {
	
	public abstract void onResponse(ReadFuture future);
}
