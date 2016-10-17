package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.codec.http11.future.ServerHttpHeaderParser;
import com.generallycloud.nio.codec.http11.future.ServerHttpReadFuture;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ServerHTTPProtocolDecoder implements ProtocolDecoder {

	private ServerHttpHeaderParser	parser			= new ServerHttpHeaderParser();

	@Override
	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {
		return new ServerHttpReadFuture(session, parser, buffer);
	}

}
