package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class WebSocketProtocolFactory implements ProtocolFactory {
	
	public static final String PROTOCOL_ID = "WebSocket";

	private int limit;

	public WebSocketProtocolFactory() {
		this(1024 * 8);
	}

	public WebSocketProtocolFactory(int limit) {
		this.limit = limit;
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return new WebSocketProtocolDecoder(limit);
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return new WebSocketProtocolEncoder();
	}

	@Override
	public String getProtocolID() {
		return PROTOCOL_ID;
	}
}
