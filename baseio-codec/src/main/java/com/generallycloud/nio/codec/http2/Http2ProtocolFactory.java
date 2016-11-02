package com.generallycloud.nio.codec.http2;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class Http2ProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new Http2ProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new Http2ProtocolEncoder();
	}

	public String getProtocolID() {
		return "FixedLength";
	}
	
}
