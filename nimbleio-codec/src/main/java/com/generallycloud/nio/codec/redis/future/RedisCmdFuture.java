package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public class RedisCmdFuture extends AbstractIOReadFuture implements RedisReadFuture{

	protected RedisCmdFuture(NIOContext context) {
		super(context);
	}

	public boolean read(IOSession session, ByteBuffer buffer) throws IOException {
		return true;
	}

	public void release() {
	}

	public RedisNode getRedisNode() {
		return null;
	}
}
