package com.gifisan.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;
import com.gifisan.nio.component.protocol.http11.future.ClientHttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.ClientHttpHeaderParser;

public class ClientHTTPProtocolDecoder implements ProtocolDecoder {

	private ByteBuffer				buffer	= ByteBuffer.allocate(1024 * 4);

	private ClientHttpHeaderParser	parser	= new ClientHttpHeaderParser();

	public IOReadFuture decode(TCPEndPoint endPoint) throws IOException {

		ByteBuffer buffer = this.buffer;

		int length = endPoint.read(buffer);

		if (length < 1) {
			if (length == -1) {
				CloseUtil.close(endPoint);
			}
			return null;
		}

		ClientHttpReadFuture future = new ClientHttpReadFuture(endPoint.getSession(), parser, buffer);

		future.decode(endPoint, buffer);

		return future;
	}

}
