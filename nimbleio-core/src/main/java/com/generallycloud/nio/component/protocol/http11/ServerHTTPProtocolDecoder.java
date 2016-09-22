package com.generallycloud.nio.component.protocol.http11;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;
import com.generallycloud.nio.component.protocol.http11.future.ServerHttpHeaderParser;
import com.generallycloud.nio.component.protocol.http11.future.ServerHttpReadFuture;

public class ServerHTTPProtocolDecoder implements ProtocolDecoder {

	private ByteBuffer				buffer			= ByteBuffer.allocate(1024 * 4);

	private ServerHttpHeaderParser	parser			= new ServerHttpHeaderParser();

	public IOReadFuture decode(SocketChannel channel) throws IOException {

		ByteBuffer buffer = this.buffer;

		int length = channel.read(buffer);

		if (length < 1) {
			if (length == -1) {
				CloseUtil.close(channel);
			}
			return null;
		}

		ServerHttpReadFuture future = new ServerHttpReadFuture(channel.getSession(), parser, buffer);

		future.decode(channel, buffer);

		return future;
	}

}
