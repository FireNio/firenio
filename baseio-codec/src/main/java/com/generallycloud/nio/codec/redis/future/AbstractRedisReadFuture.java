package com.generallycloud.nio.codec.redis.future;

import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractRedisReadFuture extends AbstractChannelReadFuture implements RedisReadFuture {

	protected AbstractRedisReadFuture(SocketChannelContext context) {
		super(context);
	}

	private BufferedOutputStream outputStream = new BufferedOutputStream();

	public void writeCommand(byte[] command, byte[]... args) {

		this.write(RedisReadFuture.BYTE_ARRAYS);
		this.writeString(String.valueOf(args.length + 1));
		this.write(RedisReadFuture.CRLF_BYTES);
		this.write(RedisReadFuture.BYTE_BULK_STRINGS);
		this.writeString(String.valueOf(command.length));
		this.write(RedisReadFuture.CRLF_BYTES);
		this.write(command);
		this.write(RedisReadFuture.CRLF_BYTES);

		for (byte[] arg : args) {
			this.write(RedisReadFuture.BYTE_BULK_STRINGS);
			this.writeString(String.valueOf(arg.length));
			this.write(RedisReadFuture.CRLF_BYTES);
			this.write(arg);
			this.write(RedisReadFuture.CRLF_BYTES);
		}
	}

	private void write(byte[] bytes) {
		outputStream.write(bytes);
	}

	private void writeString(String value) {
		outputStream.write(value.getBytes(context.getEncoding()));
	}

	private void write(byte b) {
		outputStream.write(b);
	}

	public BufferedOutputStream getBufferedOutputStream() {
		return outputStream;
	}
}
