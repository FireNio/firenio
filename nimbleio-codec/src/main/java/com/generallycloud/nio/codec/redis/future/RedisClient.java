package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.nio.codec.redis.future.RedisReadFuture.RedisCommand;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;

//FIXME check null
public class RedisClient {

	private NIOContext	context;

	private Session	session;

	public RedisClient(Session session) {
		this.session = session;
		this.context = session.getContext();
	}

	private void sendCommand(byte[] command, byte[]... args) throws IOException {

		RedisReadFuture future = new RedisCmdFuture(context);

		future.writeCommand(command, args);

		session.flush(future);
	}

	private void sendCommand(RedisCommand command, byte[]... args) throws IOException {
		sendCommand(command.raw, args);
	}

	public void set(String key, String value) throws IOException {
		byte[] _key = key.getBytes(context.getEncoding());
		byte[] _value = value.getBytes(context.getEncoding());
		sendCommand(RedisCommand.SET, _key, _value);
	}

	public void get(String key) throws IOException {
		byte[] _key = key.getBytes(context.getEncoding());
		sendCommand(RedisCommand.GET, _key);
	}
	
	public void ping() throws IOException {
		sendCommand(RedisCommand.GET);
	}

}
