package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class ServerHTTPProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new ServerHTTPProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ServerHTTPProtocolEncoder();
	}
	
	public String getProtocolID() {
		return "HTTP11";
	}
}
