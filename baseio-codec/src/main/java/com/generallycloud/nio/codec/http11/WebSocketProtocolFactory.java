package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class WebSocketProtocolFactory implements ProtocolFactory {

	private int limit;

	public WebSocketProtocolFactory() {
		this(1024 * 8);
	}

	public WebSocketProtocolFactory(int limit) {
		this.limit = limit;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return new WebSocketProtocolDecoder(limit);
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new WebSocketProtocolEncoder();
	}

	public String getProtocolID() {
		return "WebSocket";
	}
}
