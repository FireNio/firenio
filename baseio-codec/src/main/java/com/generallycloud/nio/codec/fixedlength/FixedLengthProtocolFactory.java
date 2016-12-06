package com.generallycloud.nio.codec.fixedlength;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class FixedLengthProtocolFactory implements ProtocolFactory {

	private int limit;

	public FixedLengthProtocolFactory() {
		this(1024 * 8);
	}

	public FixedLengthProtocolFactory(int limit) {
		this.limit = limit;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return new FixedLengthProtocolDecoder(limit);
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new FixedLengthProtocolEncoder();
	}

	public String getProtocolID() {
		return "FixedLength";
	}

}
