package com.generallycloud.nio.codec.line;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.codec.line.future.LineBasedReadFutureImpl;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

/**
 * 基于换行符\n的消息分割
 */
public class LineBasedProtocolDecoder implements ProtocolDecoder {

	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {
		
		return new LineBasedReadFutureImpl(session.getContext());
	}

}
