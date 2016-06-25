package com.gifisan.nio.component;

import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.client.WaiterOnReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.server.NIOContext;

public class OnReadFutureWrapper implements OnReadFuture{
	
	private OnReadFuture listener = null;
	
	private LinkedList<WaiterOnReadFuture> waiters = new LinkedListM2O<WaiterOnReadFuture>();
	
	private NIOContext context = null;

	public OnReadFutureWrapper(NIOContext context) {
		this.context = context;
	}

	public void onResponse(final FixedSession session, final ReadFuture future) {
		
		WaiterOnReadFuture waiter = waiters.poll();
		
		if (waiter != null) {
			
			waiter.onResponse(session, future);
			
			return;
		}
		
		if (listener == null) {
			return;
		}
		
		ThreadPool pool = context.getThreadPool();
		
		pool.dispatch(new Runnable() {
			
			public void run() {
				listener.onResponse(session, future);
			}
		});
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
