package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpIOEventHandle extends IOEventHandleAdaptor{
	
	private Waiter<HttpReadFuture> waiter;

	public void accept(Session session, ReadFuture future) throws Exception {
		
		HttpReadFuture f = (HttpReadFuture) future;
		
		Waiter<HttpReadFuture> waiter = this.waiter;
		
		if (waiter != null) {
			
			this.waiter = null;
			
			waiter.setPayload(f);
		}
	}

	public void setWaiter(Waiter<HttpReadFuture> waiter) {
		this.waiter = waiter;
	}
}
