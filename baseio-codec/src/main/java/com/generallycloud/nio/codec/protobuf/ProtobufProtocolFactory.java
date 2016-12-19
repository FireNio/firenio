package com.generallycloud.nio.codec.protobuf;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ProtobufProtocolFactory implements ProtocolFactory {

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return new ProtobufProtocolDecoder();
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return new ProtobufProtocolEncoder();
	}

	@Override
	public String getProtocolID() {
		return "Protobuf";
	}

}
