package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class WebSocketProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new WebSocketProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new WebSocketProtocolEncoder();
	}
	
	public String getProtocolID() {
		return "WebSocket";
	}
}
