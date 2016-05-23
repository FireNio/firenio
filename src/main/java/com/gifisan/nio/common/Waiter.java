package com.gifisan.nio.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Waiter<T> {

	private CountDownLatch	latch	= new CountDownLatch(1);
	private T				t		= null;

	public boolean await(long timeout) {
		try {
			return latch.await(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			return false;
		}
	}

	public void setPayload(T t) {
		this.t = t;
		this.latch.countDown();
	}

	public T getPayload() {
		return t;
	}

}
