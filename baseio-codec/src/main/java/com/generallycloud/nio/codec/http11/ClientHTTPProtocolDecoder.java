package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.future.ClientHttpReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ClientHTTPProtocolDecoder implements ProtocolDecoder {

	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		return new ClientHttpReadFuture(session, buffer);
	}

}
