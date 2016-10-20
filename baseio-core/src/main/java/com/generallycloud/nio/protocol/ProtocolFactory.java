package com.generallycloud.nio.protocol;


public interface ProtocolFactory {

	public abstract ProtocolDecoder getProtocolDecoder();
	
	public abstract ProtocolEncoder getProtocolEncoder();
	
	public abstract String getProtocolID();
}
