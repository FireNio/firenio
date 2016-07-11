package com.gifisan.nio.component.future;


public interface WriteFuture extends Future{

	public abstract ReadFuture getReadFuture();
}
