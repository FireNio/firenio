package com.gifisan.nio.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.ReadFuture;

public class MessageBus {

	private ReadFuture		future	= null;
	private ReentrantLock	lock		= new ReentrantLock();
	private Condition		notNull	= lock.newCondition();
	private OnReadFuture 	onReadFuture = null;

	public ReadFuture poll(long timeout) {
		if (future == null) {
			ReentrantLock lock = this.lock;

			lock.lock();

			try {
				notNull.await(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
				notNull.signal();
			}

			lock.unlock();

			return future;
		}
		return future;
	}

	public void offer(ReadFuture future) {
		
		if (onReadFuture == null) {
			DefaultClientSession session = (DefaultClientSession) ((IOReadFuture)future).getSession(); 
			try {
				onReadFuture.onResponse(session, future);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			onReadFuture = null;
			session.offer();
			return;
		}
		
		ReentrantLock lock = this.lock;

		lock.lock();
		
		this.future = future;

		notNull.signal();

		lock.unlock();

	}

}
