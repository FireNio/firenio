package com.gifisan.nio.extend;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.OnReadFuture;

public class UpdateSessionIDOnReadFuture implements OnReadFuture{

	public void onResponse(FixedSession session, ReadFuture future) {
		session.getSession().setSessionID(Long.valueOf(future.getText()));
	}
	
}
