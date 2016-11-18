package com.generallycloud.nio.codec.redis;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.codec.redis.future.RedisReadFuture;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.ProtocolEncoder;

public class RedisProtocolEncoder implements ProtocolEncoder {

	public ChannelWriteFuture encode(ByteBufAllocator allocator, ChannelReadFuture future) throws IOException {
		
		RedisReadFuture f = (RedisReadFuture) future;

		BufferedOutputStream os = f.getBufferedOutputStream();
		
		int size = os.size();
		
		if (size == 0) {
			throw new IOException("null write text");
		}

		ByteBuf buf = allocator.allocate(size);

		buf.put(os.array(), 0, size);

		return new ChannelWriteFutureImpl(future, buf.flip());
		
	}
	
}
