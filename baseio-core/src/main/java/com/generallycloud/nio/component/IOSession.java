package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public interface IOSession extends Session{
	
	public abstract void fireOpend();

	public abstract void flush(IOWriteFuture future) throws IOException;

	public abstract ProtocolDecoder getProtocolDecoder() ;

	public abstract ProtocolEncoder getProtocolEncoder() ;

	public abstract ProtocolFactory getProtocolFactory() ;
	
	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder) ;

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder) ;
	
	public abstract void setProtocolFactory(ProtocolFactory protocolFactory) ;
	
}
