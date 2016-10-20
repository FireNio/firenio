package com.generallycloud.nio.codec.protobuf;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ProtobufProtocolFactory implements ProtocolFactory {

	public ProtocolDecoder getProtocolDecoder() {
		return new ProtobufProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ProtobufProtocolEncoder();
	}

	public String getProtocolID() {
		return "Protobuf";
	}

}
