package com.gifisan.nio.extend;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.LinkedList;
import com.gifisan.nio.component.concurrent.LinkedListABQ;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public class OnReadFutureWrapper implements OnReadFuture{
	
	private OnReadFuture listener = null;
	
	private LinkedList<WaiterOnReadFuture> waiters = new LinkedListABQ<WaiterOnReadFuture>(1024 * 8);
	
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
