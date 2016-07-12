package com.gifisan.nio.component.protocol;


public interface ProtocolFactory {

	public abstract ProtocolDecoder getProtocolDecoder();
	
	public abstract ProtocolEncoder getProtocolEncoder();
}
