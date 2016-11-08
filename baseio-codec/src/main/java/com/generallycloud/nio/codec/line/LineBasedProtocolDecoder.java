package com.generallycloud.nio.codec.line;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.line.future.LineBasedReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * 基于换行符\n的消息分割
 */
public class LineBasedProtocolDecoder implements ProtocolDecoder {

	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {
		
		return new LineBasedReadFutureImpl(session.getContext());
	}

}
