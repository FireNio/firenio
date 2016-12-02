package com.generallycloud.nio.codec.protobase;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ProtobaseProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new ProtobaseProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ProtobaseProtocolEncoder();
	}

	public String getProtocolID() {
		return "NIO";
	}
}
