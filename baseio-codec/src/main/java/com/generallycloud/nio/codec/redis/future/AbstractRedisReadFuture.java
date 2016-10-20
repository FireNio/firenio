package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public abstract class AbstractRedisReadFuture extends AbstractIOReadFuture implements RedisReadFuture{

	protected AbstractRedisReadFuture(BaseContext context) {
		super(context);
	}

	public void writeCommand(byte[] command, byte[]... args) {

		this.write(RedisReadFuture.BYTE_ARRAYS);
		this.write(String.valueOf(args.length + 1));
		this.write(RedisReadFuture.CRLF_BYTES);
		this.write(RedisReadFuture.BYTE_BULK_STRINGS);
		this.write(String.valueOf(command.length));
		this.write(RedisReadFuture.CRLF_BYTES);
		this.write(command);
		this.write(RedisReadFuture.CRLF_BYTES);

		for (byte[] arg : args) {
			this.write(RedisReadFuture.BYTE_BULK_STRINGS);
			this.write(String.valueOf(arg.length));
			this.write(RedisReadFuture.CRLF_BYTES);
			this.write(arg);
			this.write(RedisReadFuture.CRLF_BYTES);
		}
	}
	
}
