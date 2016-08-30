package com.generallycloud.nio.common.test;

import java.util.concurrent.CountDownLatch;

public abstract class ITestThread implements Runnable{

	private CountDownLatch latch;
	
	private int time;

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}
	
	public abstract void prepare() throws Exception;
	
	public abstract void stop();
	
}
