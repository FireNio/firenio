package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface OnReadFuture {
	
	public abstract void onResponse(Session session ,ReadFuture future);
}
