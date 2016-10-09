package com.generallycloud.nio.component;

import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ReadFuture;

public class WaiterOnReadFuture implements OnReadFuture {

	private Waiter<ReadFuture>	waiter	= new Waiter<ReadFuture>();

	
	/**
	 * @param timeout
	 * @return timeouted
	 */
	public boolean await(long timeout) {
		return waiter.await(timeout);
	}

	public ReadFuture getReadFuture() {
		return waiter.getPayload();
	}

	public void onResponse(Session session, ReadFuture future) {
		this.waiter.setPayload(future);
	}
}
