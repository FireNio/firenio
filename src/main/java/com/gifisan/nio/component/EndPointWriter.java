package com.gifisan.nio.component;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.future.IOWriteFuture;

public interface EndPointWriter extends LifeCycle, Runnable{

	public abstract void collect();

	public abstract void offer(IOWriteFuture future);

}