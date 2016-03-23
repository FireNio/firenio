package com.gifisan.nio.server;

public interface InnerResponse extends Response{

	public abstract Response update() ;
	
	public abstract boolean flushed();
	
	public abstract boolean schduled();
	
}
