package com.generallycloud.nio.component.protocol.http11;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

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
