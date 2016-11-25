package com.generallycloud.test.nio.redis;

import com.generallycloud.nio.codec.redis.RedisProtocolFactory;
import com.generallycloud.nio.codec.redis.future.RedisClient;
import com.generallycloud.nio.codec.redis.future.RedisIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class TestRedisClient {

	public static void main(String[] args) throws Exception {
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration(6379));

		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setIoEventHandleAdaptor(new RedisIOEventHandle());

		context.addSessionEventListener(new LoggerSocketSEListener());

		context.setProtocolFactory(new RedisProtocolFactory());

		SocketSession session = connector.connect();

		RedisClient client = new RedisClient(session);

		String value = client.set("name222", "hello redis!");

		System.out.println("__________________res______" + value);

		value = client.get("name222");

		System.out.println("__________________res______" + value);

		value = client.set("debug", "PONG");

		System.out.println("__________________res______" + value);

		value = client.get("debug");

		System.out.println("__________________res______" + value);

		value = client.ping();

		System.out.println("__________________res______" + value);

		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
