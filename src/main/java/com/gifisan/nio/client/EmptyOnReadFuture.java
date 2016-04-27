package com.gifisan.nio.client;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(Session sesssion, ReadFuture future) {
		
	}
	
}
