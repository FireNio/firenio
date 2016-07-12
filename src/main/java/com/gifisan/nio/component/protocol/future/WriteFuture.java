package com.gifisan.nio.component.protocol.future;


public interface WriteFuture extends Future{

	public abstract ReadFuture getReadFuture();
}
