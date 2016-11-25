package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface OnReadFuture {
	
	public abstract void onResponse(SocketSession session ,ReadFuture future);
}
