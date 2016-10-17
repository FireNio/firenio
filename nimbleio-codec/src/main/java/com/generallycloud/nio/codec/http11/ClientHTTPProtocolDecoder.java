package com.generallycloud.nio.codec.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.codec.http11.future.ClientHttpHeaderParser;
import com.generallycloud.nio.codec.http11.future.ClientHttpReadFuture;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class ClientHTTPProtocolDecoder implements ProtocolDecoder {

	private ClientHttpHeaderParser	parser	= new ClientHttpHeaderParser();

	@Override
	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {

		return new ClientHttpReadFuture(session, parser, buffer);
	}

}
