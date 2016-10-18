package com.generallycloud.nio.codec.redis;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.codec.redis.future.RedisReadFutureImpl;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOReadFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;

public class RedisProtocolDecoder implements ProtocolDecoder{

	public IOReadFuture decode(IOSession session, ByteBuffer buffer) throws IOException {
		return new RedisReadFutureImpl(session.getContext());
	}
	
}
