package com.generallycloud.nio.codec.redis;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.IOWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class RedisProtocolEncoder implements ProtocolEncoder {

	public IOWriteFuture encode(IOSession session, IOReadFuture future) throws IOException {

		BufferedOutputStream os = future.getWriteBuffer();

		int size = os.size();

		byte[] array = os.array();

		ByteBuf buf = session.getContext().getHeapByteBufferPool().allocate(size);

		buf.put(array, 0, size);

		buf.flip();

		return new IOWriteFutureImpl(session, future, buf);
	}
}
