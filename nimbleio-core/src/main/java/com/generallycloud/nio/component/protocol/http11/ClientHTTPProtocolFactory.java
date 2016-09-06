package com.generallycloud.nio.component.protocol.http11;

import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.ProtocolEncoder;
import com.generallycloud.nio.component.protocol.ProtocolFactory;

public class ClientHTTPProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new ClientHTTPProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ClientHTTPProtocolEncoder();
	}
	
	public String getProtocolID() {
		return "HTTP11";
	}
}
