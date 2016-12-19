package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;

public class RedisCmdFuture extends AbstractRedisReadFuture {

	protected RedisCmdFuture(SocketChannelContext context) {
		super(context);
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {
		return true;
	}

	@Override
	public void release() {
	}

	@Override
	public RedisNode getRedisNode() {
		return null;
	}

}
