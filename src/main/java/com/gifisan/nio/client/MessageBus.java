package com.gifisan.nio.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListO2O;

public class MessageBus {

	private ReadFuture				future		= null;
	private ReentrantLock			lock			= new ReentrantLock();
	private Condition				notNull		= lock.newCondition();
	private LinkedList<OnReadFuture>	onReadFutures	= new LinkedListO2O<OnReadFuture>(1024 * 10);

	public ReadFuture poll(long timeout) {
		if (timeout == 0) {

			for (;;) {

				ReentrantLock lock = this.lock;

				lock.lock();

				try {
					if (future == null) {
						notNull.await(16, TimeUnit.MILLISECONDS);
					}

					if (future == null) {
						continue;
					}

					ReadFuture _Future = this.future;

					this.future = null;

					return _Future;
				} catch (InterruptedException e) {
					e.printStackTrace();
					notNull.signal();
				} finally {
					lock.unlock();
				}

				continue;
			}
		}

		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			if (future == null) {
				notNull.await(timeout, TimeUnit.MILLISECONDS);
			}

			ReadFuture _Future = this.future;

			this.future = null;

			return _Future;
		} catch (InterruptedException e) {
			e.printStackTrace();
			notNull.signal();
			return null;
		} finally {
			lock.unlock();
		}
	}

	
	public void offer(ReadFuture future) {

		OnReadFuture onReadFuture = this.onReadFutures.poll();

		if (onReadFuture != null) {
			ProtectedClientSession session = (ProtectedClientSession) ((IOReadFuture) future).getSession();
			try {
				onReadFuture.onResponse(session, future);
			} catch (Exception e) {
				e.printStackTrace();
			}
			session.offer();
			return;
		}
		
		ReentrantLock lock = this.lock;

		lock.lock();

		this.future = future;

		notNull.signal();

		lock.unlock();
	}

	public void onReadFuture(OnReadFuture onReadFuture) {
		this.onReadFutures.forceOffer(onReadFuture);
	}

	public void reset() {
		this.future = null;
	}

}
