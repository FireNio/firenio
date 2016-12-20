/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.codec.redis.future;

import java.io.IOException;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.codec.redis.future.RedisReadFuture.RedisCommand;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;

//FIXME check null
public class RedisClient {

	private SocketChannelContext	context;

	private SocketSession		session;

	private RedisIOEventHandle	ioEventHandle;

	private long				timeout;

	public RedisClient(SocketSession session) {
		this(session, 3000);
	}

	public RedisClient(SocketSession session, long timeout) {
		this.timeout = timeout;
		this.session = session;
		this.context = session.getContext();
		this.ioEventHandle = (RedisIOEventHandle) context.getIoEventHandleAdaptor();
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
