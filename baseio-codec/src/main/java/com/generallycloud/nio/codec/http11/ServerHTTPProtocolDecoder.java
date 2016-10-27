package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.codec.http11.future.ServerHttpReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ServerHTTPProtocolDecoder implements ProtocolDecoder {

	@Override
	public IOReadFuture decode(SocketSession session, ByteBuffer buffer) throws IOException {
		return new ServerHttpReadFuture(session, buffer);
	}

}
