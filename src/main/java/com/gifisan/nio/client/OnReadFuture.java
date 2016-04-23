package com.gifisan.nio.client;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.server.session.Session;

public interface OnReadFuture {
	
	public abstract void onResponse(Session sesssion ,ReadFuture future);
}
