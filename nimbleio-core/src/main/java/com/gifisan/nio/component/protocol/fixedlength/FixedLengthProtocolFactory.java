package com.gifisan.nio.component.protocol.fixedlength;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolFactory;

public class FixedLengthProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new FixedLengthProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new FixedLengthProtocolEncoder();
	}

	
}
