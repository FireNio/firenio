package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;

public class RedisCmdFuture extends AbstractRedisReadFuture implements RedisReadFuture {

	protected RedisCmdFuture(BaseContext context) {
		super(context);
	}

	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {
		return true;
	}

	public void release() {
	}

	public RedisNode getRedisNode() {
		return null;
	}


}
