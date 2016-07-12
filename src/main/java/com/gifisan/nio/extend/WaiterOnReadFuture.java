package com.gifisan.nio.extend;

import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class WaiterOnReadFuture implements OnReadFuture {

	private Waiter<NIOReadFuture>	waiter	= new Waiter<NIOReadFuture>();

	public boolean await(long timeout) {
		return waiter.await(timeout);
	}

	public NIOReadFuture getReadFuture() {
		return waiter.getPayload();
	}

	public void onResponse(FixedSession session, NIOReadFuture future) {
		this.waiter.setPayload(future);
	}
}
