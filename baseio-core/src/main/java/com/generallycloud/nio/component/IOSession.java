package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public interface IOSession extends Session{
	
	public abstract boolean isEnableSSL();
	
	public abstract SSLEngine getSSLEngine();
	
	public abstract void setSSLEngine(SSLEngine engine);
	
	public abstract SslHandler getSslHandler();
	
	public abstract void fireOpend();

	public abstract void flush(IOWriteFuture future);

	public abstract ProtocolDecoder getProtocolDecoder() ;

	public abstract ProtocolEncoder getProtocolEncoder() ;

	public abstract ProtocolFactory getProtocolFactory() ;
	
	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder) ;

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder) ;
	
	public abstract void setProtocolFactory(ProtocolFactory protocolFactory) ;
	
}
