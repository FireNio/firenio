package com.generallycloud.nio.codec.linebased;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class LineBasedProtocolFactory implements ProtocolFactory{

	private int limit;

	public LineBasedProtocolFactory() {
		this(1024 * 8);
	}

	public LineBasedProtocolFactory(int limit) {
		this.limit = limit;
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return new LineBasedProtocolDecoder(limit);
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return new LineBasedProtocolEncoder();
	}

	@Override
	public String getProtocolID() {
		return "LineBased";
	}
	
}
