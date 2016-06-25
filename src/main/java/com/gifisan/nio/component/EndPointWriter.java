package com.gifisan.nio.component;

import com.gifisan.nio.Looper;
import com.gifisan.nio.component.future.IOWriteFuture;

public interface EndPointWriter extends Looper{
	
	public abstract void collect();

	public abstract void offer(IOWriteFuture future);

}