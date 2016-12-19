package com.generallycloud.nio.codec.linebased;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.linebased.future.LineBasedReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * 基于换行符\n的消息分割
 */
public class LineBasedProtocolDecoder implements ProtocolDecoder {

	private int limit;

	public LineBasedProtocolDecoder(int limit) {
		this.limit = limit;
	}

	@Override
	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {

		return new LineBasedReadFutureImpl(session.getContext(), limit);
	}

}
