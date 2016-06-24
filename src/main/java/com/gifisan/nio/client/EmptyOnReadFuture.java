package com.gifisan.nio.client;

import com.gifisan.nio.component.future.ReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(FixedSession session, ReadFuture future) {
		
	}
	
}
