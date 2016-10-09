package com.generallycloud.nio.protocol;

public interface NamedReadFuture extends ReadFuture{

	public abstract String getFutureName();
}
