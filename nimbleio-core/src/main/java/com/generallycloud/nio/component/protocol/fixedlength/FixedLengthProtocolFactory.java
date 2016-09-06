package com.generallycloud.nio.component.protocol.fixedlength;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

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
