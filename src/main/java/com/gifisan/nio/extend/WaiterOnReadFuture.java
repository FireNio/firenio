package com.gifisan.nio.extend;

import com.gifisan.nio.component.concurrent.Waiter;
import com.gifisan.nio.component.future.ReadFuture;

public class WaiterOnReadFuture implements OnReadFuture {

	private Waiter<ReadFuture>	waiter	= new Waiter<ReadFuture>();

	public boolean await(long timeout) {
		return waiter.await(timeout);
	}

	public ReadFuture getReadFuture() {
		return waiter.getPayload();
	}

	public void onResponse(FixedSession session, ReadFuture future) {
		this.waiter.setPayload(future);
	}
}
