package com.gifisan.nio.extend;

import com.gifisan.nio.component.future.ReadFuture;

public class UpdateSessionIDOnReadFuture implements OnReadFuture{

	public void onResponse(FixedSession session, ReadFuture future) {
		session.getSession().setSessionID(Integer.valueOf(future.getText()));
	}
	
}
