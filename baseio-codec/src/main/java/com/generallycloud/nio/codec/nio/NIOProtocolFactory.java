package com.generallycloud.nio.codec.nio;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class NIOProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new NIOProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new NIOProtocolEncoder();
	}

	public String getProtocolID() {
		return "NIO";
	}
}
