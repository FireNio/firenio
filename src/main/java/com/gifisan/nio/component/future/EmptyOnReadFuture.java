package com.gifisan.nio.component.future;

import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;

public class EmptyOnReadFuture implements OnReadFuture{
	
	public void onResponse(FixedSession session, ReadFuture future) {
		
	}
	
}
