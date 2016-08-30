package com.generallycloud.nio.component;

import com.generallycloud.nio.component.protocol.ReadFuture;

public interface OnReadFuture {
	
	public abstract void onResponse(Session session ,ReadFuture future);
}
