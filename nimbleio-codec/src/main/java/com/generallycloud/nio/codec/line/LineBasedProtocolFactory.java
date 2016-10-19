package com.generallycloud.nio.codec.line;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class LineBasedProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new LineBasedProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new LineBasedProtocolEncoder();
	}

	public String getProtocolID() {
		return "LineBased";
	}
	
}
