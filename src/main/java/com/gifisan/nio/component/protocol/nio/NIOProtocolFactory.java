package com.gifisan.nio.component.protocol.nio;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolFactory;

public class NIOProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new ServerNIOProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new NIOProtocolEncoder();
	}

	
}
