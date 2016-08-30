package com.generallycloud.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.http11.future.ClientHttpHeaderParser;
import com.generallycloud.nio.component.protocol.http11.future.ClientHttpReadFuture;

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
