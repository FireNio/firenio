package com.gifisan.nio.component.future;

import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.extend.FixedSession;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(FixedSession session, ReadFuture future) {
		
	}
	
}
