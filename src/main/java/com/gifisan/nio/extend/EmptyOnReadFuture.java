package com.gifisan.nio.extend;

import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(FixedSession session, NIOReadFuture future) {
		
	}
	
}
