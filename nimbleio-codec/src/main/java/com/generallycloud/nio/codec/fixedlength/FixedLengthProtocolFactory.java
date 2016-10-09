package com.generallycloud.nio.codec.fixedlength;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class FixedLengthProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new FixedLengthProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new FixedLengthProtocolEncoder();
	}

	public String getProtocolID() {
		return "FixedLength";
	}
	
}
