package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.codec.redis.future.RedisReadFuture.RedisCommand;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.Waiter;

//FIXME check null
public class RedisClient {

	private BaseContext			context;

	private Session			session;

	private RedisIOEventHandle	ioEventHandle;

	private long				timeout;

	public RedisClient(Session session) {
		this(session, 3000);
	}

	public RedisClient(Session session, long timeout) {
		this.timeout = timeout;
		this.session = session;
		this.context = session.getContext();
		this.ioEventHandle = (RedisIOEventHandle) context.getIOEventHandleAdaptor();
	}

	private synchronized RedisNode sendCommand(byte[] command, byte[]... args) throws IOException {

		RedisReadFuture future = new RedisCmdFuture(context);

		future.writeCommand(command, args);

		Waiter<RedisNode> waiter = new Waiter<RedisNode>();

		ioEventHandle.setWaiter(waiter);

		session.flush(future);

		if (waiter.await(timeout)) {
			throw new TimeoutException("timeout");
		}

		return waiter.getPayload();
	}

	private RedisNode sendCommand(RedisCommand command, byte[]... args) throws IOException {
		return sendCommand(command.raw, args);
	}

	public String set(String key, String value) throws IOException {
		byte[] _key = key.getBytes(context.getEncoding());
		byte[] _value = value.getBytes(context.getEncoding());
		RedisNode node = sendCommand(RedisCommand.SET, _key, _value);
		return (String) node.getValue();
	}

	public String get(String key) throws IOException {
		byte[] _key = key.getBytes(context.getEncoding());
		RedisNode node = sendCommand(RedisCommand.GET, _key);
		return (String) node.getValue();
	}

	public String ping() throws IOException {
		RedisNode node = sendCommand(RedisCommand.PING);
		return (String) node.getValue();
	}

}
