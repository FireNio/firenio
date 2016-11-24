package com.generallycloud.nio.protocol;

public interface DatagramReadFuture extends ReadFuture{

	public abstract DatagramReadFuture newDatagramReadFuture();
	
}
