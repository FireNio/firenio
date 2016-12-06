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

	public ProtocolDecoder getProtocolDecoder() {
		return new LineBasedProtocolDecoder(limit);
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new LineBasedProtocolEncoder();
	}

	public String getProtocolID() {
		return "LineBased";
	}
	
}
