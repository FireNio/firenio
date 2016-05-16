package com.gifisan.nio.server;

import java.nio.charset.Charset;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.Attributes;
import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.UDPEndPointFactory;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;

public interface NIOContext extends Attributes, LifeCycle {

	public abstract Charset getEncoding();

	public abstract ProtocolDecoder getProtocolDecoder();
	
	public abstract ProtocolEncoder getProtocolEncoder();
	
	public abstract ReadFutureAcceptor getReadFutureAcceptor();
	
	public abstract OutputStreamAcceptor getOutputStreamAcceptor() ;
	
	public abstract DatagramPacketAcceptor getDatagramPacketAcceptor();
	
	public abstract UDPEndPointFactory getUDPEndPointFactory();
	
}