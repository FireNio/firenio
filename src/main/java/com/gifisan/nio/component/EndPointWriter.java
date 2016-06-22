package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOWriteFuture;

public interface EndPointWriter extends Runnable{
	
	public abstract void collect();

	public abstract void offer(IOWriteFuture future);

}