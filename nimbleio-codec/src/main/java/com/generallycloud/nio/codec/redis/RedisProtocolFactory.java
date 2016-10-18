package com.generallycloud.nio.codec.redis;

import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public class RedisProtocolFactory implements ProtocolFactory {

	public ProtocolDecoder getProtocolDecoder() {
		return new RedisProtocolDecoder();
	}

	public ProtocolEncoder getProtocolEncoder() {
		return new RedisProtocolEncoder();
	}

	public String getProtocolID() {
		return "Redis";
	}

}
