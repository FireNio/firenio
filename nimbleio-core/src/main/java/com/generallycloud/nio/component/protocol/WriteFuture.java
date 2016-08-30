package com.generallycloud.nio.component.protocol;


public interface WriteFuture extends Future{

	public abstract ReadFuture getReadFuture();
}
