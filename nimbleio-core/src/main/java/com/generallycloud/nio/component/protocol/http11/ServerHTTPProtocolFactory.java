package com.generallycloud.nio.component.protocol.http11;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

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
