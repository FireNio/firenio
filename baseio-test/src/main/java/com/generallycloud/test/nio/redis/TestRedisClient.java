package com.generallycloud.test.nio.redis;

import com.generallycloud.nio.codec.redis.RedisProtocolFactory;
import com.generallycloud.nio.codec.redis.future.RedisClient;
import com.generallycloud.nio.codec.redis.future.RedisIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class TestRedisClient {

	public static void main(String[] args) throws Exception {
		
		BaseContext context = new BaseContextImpl(new ServerConfiguration(6379));

		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setIoEventHandleAdaptor(new RedisIOEventHandle());

		context.addSessionEventListener(new LoggerSEListener());

		context.setProtocolFactory(new RedisProtocolFactory());

		Session session = connector.connect();

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
