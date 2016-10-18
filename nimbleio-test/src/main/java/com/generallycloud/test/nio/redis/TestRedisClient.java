package com.generallycloud.test.nio.redis;

import com.generallycloud.nio.codec.redis.RedisProtocolFactory;
import com.generallycloud.nio.codec.redis.future.RedisClient;
import com.generallycloud.nio.codec.redis.future.RedisIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.ConnectorCloseSEListener;

public class TestRedisClient {

	public static void main(String[] args) throws Exception {

		SocketChannelConnector connector = new SocketChannelConnector();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("localhost");
		configuration.setSERVER_TCP_PORT(6379);
		
		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent", 
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				1);

		NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

		context.setIOEventHandleAdaptor(new RedisIOEventHandle());
		
		context.addSessionEventListener(new LoggerSEListener());

		context.addSessionEventListener(new ConnectorCloseSEListener(connector));

		context.setProtocolFactory(new RedisProtocolFactory());
		
		connector.setContext(context);
		
		Session session = connector.connect();

		RedisClient client = new RedisClient(session);
		
		String value = client.set("name222", "hello redis!");
		
		System.out.println("__________________res______"+value);
		
		value = client.get("name222");
		
		System.out.println("__________________res______"+value);
		
		value = client.set("debug", "PONG");
		
		System.out.println("__________________res______"+value);
		
		value = client.get("debug");
		
		System.out.println("__________________res______"+value);
		
		value = client.ping();
		
		System.out.println("__________________res______"+value);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
