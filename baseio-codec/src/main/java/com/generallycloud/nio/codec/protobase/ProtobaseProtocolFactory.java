package com.generallycloud.nio.codec.protobase;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ProtobaseProtocolFactory implements ProtocolFactory {

	private int limit;

	public ProtobaseProtocolFactory() {
		this(1024 * 8);
	}

	public ProtobaseProtocolFactory(int limit) {
		this.limit = limit;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return new ProtobaseProtocolDecoder(limit);
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ProtobaseProtocolEncoder();
	}

	public String getProtocolID() {
		return "Protobase";
	}
}
