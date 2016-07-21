package com.gifisan.nio.component.protocol.http11;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.ProtocolEncoder;
import com.gifisan.nio.component.protocol.ProtocolFactory;

public class ClientHTTPProtocolFactory implements ProtocolFactory{

	public ProtocolDecoder getProtocolDecoder() {
		return new ClientHTTPProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new ClientHTTPProtocolEncoder();
	}
}
