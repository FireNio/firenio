package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpIOEventHandle extends IoEventHandleAdaptor{
	
	private Waiter<HttpReadFuture> waiter;

	public void accept(SocketSession session, ReadFuture future) throws Exception {
		
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
