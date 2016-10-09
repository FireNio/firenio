package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.WaiterOnReadFuture;
import com.generallycloud.nio.component.concurrent.ListQueue;
import com.generallycloud.nio.component.concurrent.ListQueueABQ;
import com.generallycloud.nio.protocol.ReadFuture;

public class OnReadFutureWrapper implements OnReadFuture{
	
	private OnReadFuture listener = null;
	
	private ListQueue<WaiterOnReadFuture> waiters = new ListQueueABQ<WaiterOnReadFuture>(1024 * 8);
	
	public void onResponse(final Session session, final ReadFuture future) {
		
		WaiterOnReadFuture waiter = waiters.poll();
		
		if (waiter != null) {
			
			waiter.onResponse(session, future);
			
			return;
		}
		
		if (listener == null) {
			return;
		}
		
		listener.onResponse(session, future);
	}
	
	public void listen(WaiterOnReadFuture onReadFuture){
		this.waiters.offer(onReadFuture);
	}

	public OnReadFuture getListener() {
		return listener;
	}

	public void setListener(OnReadFuture listener) {
		this.listener = listener;
	}
}
