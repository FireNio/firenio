package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ServerHTTPProtocolFactory implements ProtocolFactory{

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return new ServerHTTPProtocolDecoder();
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return new ServerHTTPProtocolEncoder();
	}
	
	@Override
	public String getProtocolID() {
		return "HTTP1.1";
	}
}
