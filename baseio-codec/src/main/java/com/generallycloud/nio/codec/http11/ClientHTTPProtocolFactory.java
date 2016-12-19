package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ClientHTTPProtocolFactory implements ProtocolFactory{

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return new ClientHTTPProtocolDecoder();
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return new ClientHTTPProtocolEncoder();
	}
	
	@Override
	public String getProtocolID() {
		return "HTTP11";
	}
}
