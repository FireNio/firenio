package com.generallycloud.nio.protocol;


public interface WriteFuture extends Future{

	public abstract ReadFuture getReadFuture();
}
