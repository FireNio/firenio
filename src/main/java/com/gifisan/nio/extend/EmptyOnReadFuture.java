package com.gifisan.nio.extend;

import com.gifisan.nio.component.future.nio.NIOReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(FixedSession session, NIOReadFuture future) {
		
	}
	
}
