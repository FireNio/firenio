package com.gifisan.mtp.server;

public interface InnerResponse extends Response{

	public abstract Response update() ;
	
	public abstract boolean flushed();
	
}
