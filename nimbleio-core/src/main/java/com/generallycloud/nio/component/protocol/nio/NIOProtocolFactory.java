package com.generallycloud.nio.component.protocol.nio;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

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
