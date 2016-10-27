package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.BaseContext;

public class RedisCmdFuture extends AbstractRedisReadFuture implements RedisReadFuture {

	protected RedisCmdFuture(BaseContext context) {
		super(context);
	}

	public boolean read(SocketSession session, ByteBuffer buffer) throws IOException {
		return true;
	}

	public void release() {
	}

	public RedisNode getRedisNode() {
		return null;
	}


}
