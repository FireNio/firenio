package com.gifisan.nio.client;

import com.gifisan.nio.component.future.ReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(ClientSession session, ReadFuture future) {
		
	}
	
}
