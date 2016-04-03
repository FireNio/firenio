package com.gifisan.nio.component;

import com.gifisan.nio.component.protocol.Decoder;

public interface ProtocolData {
	
	public abstract String getText();

	public abstract byte getProtocolType() ;
	
	public abstract byte getSessionID();
	
	public abstract void setText(String text) ;
	
	public abstract void setProtocolType(byte protocolType) ;
	
	public abstract void setSessionID(byte sessionID) ;
	
	public abstract byte[] getHeader() ;

	public abstract void setHeader(byte[] header) ;

	public abstract Decoder getDecoder() ;

	public abstract void setDecoder(Decoder decoder) ;
	
	public abstract String getServiceName() ;

	public abstract void setServiceName(String serviceName) ;
	
}
