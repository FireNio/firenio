package com.generallycloud.nio.codec.redis;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.redis.future.RedisReadFutureImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class RedisProtocolDecoder implements ProtocolDecoder{

	public ChannelReadFuture decode(SocketSession session, ByteBuf buffer) throws IOException {
		return new RedisReadFutureImpl(session.getContext());
	}
	
}
