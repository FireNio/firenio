package com.gifisan.nio.component;

import com.gifisan.nio.LifeCycle;

public interface EndPointWriter1 extends LifeCycle, Runnable{

	public abstract void collect();

	public abstract void offer(IOWriteFuture future);

}