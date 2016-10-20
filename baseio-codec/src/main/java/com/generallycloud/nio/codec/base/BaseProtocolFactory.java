package com.generallycloud.nio.codec.base;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class BaseProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new BaseProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new BaseProtocolEncoder();
	}

	public String getProtocolID() {
		return "NIO";
	}
}
