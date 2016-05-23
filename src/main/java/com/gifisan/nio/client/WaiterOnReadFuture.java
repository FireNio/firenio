package com.gifisan.nio.client;

import com.gifisan.nio.common.Waiter;
import com.gifisan.nio.component.future.ReadFuture;

public class WaiterOnReadFuture implements OnReadFuture {

	private Waiter<ReadFuture>	waiter	= new Waiter<ReadFuture>();

	public void onResponse(ClientSession session, ReadFuture future) {
		this.waiter.setPayload(future);
	}

	public boolean await(long timeout) {
		return waiter.await(timeout);
	}

	public ReadFuture getReadFuture() {
		return waiter.getPayload();
	}

}
