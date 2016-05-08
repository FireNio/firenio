package com.gifisan.nio.server;

import java.nio.charset.Charset;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.Attributes;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;

public interface NIOContext extends Attributes, LifeCycle {

	public abstract Charset getEncoding();

	public abstract void setEncoding(Charset encoding);

	public abstract ProtocolDecoder getProtocolDecoder();
	
	public abstract ProtocolEncoder getProtocolEncoder();
	
//	public abstract EndPointWriter getEndPointWriter();
	
	public abstract ReadFutureAcceptor getReadFutureAcceptor();
	
	public abstract SessionFactory getSessionFactory();
	
	public abstract OutputStreamAcceptor getOutputStreamAcceptor() ;
	
}