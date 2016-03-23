package com.gifisan.nio.component;

public interface ProtocolData {
	
	public abstract String getText();

	public abstract InputStream getInputStream();
	
	public abstract byte getProtocolType() ;
	
	public abstract byte getSessionID();
	
	public abstract void setText(String text) ;
	
	public abstract void setInputStream(InputStream inputStream);

	public abstract void setProtocolType(byte protocolType) ;
	
	public abstract void setSessionID(byte sessionID) ;
	
}
