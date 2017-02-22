/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.test.nio.redis;

import com.generallycloud.nio.codec.redis.RedisProtocolFactory;
import com.generallycloud.nio.codec.redis.future.RedisClient;
import com.generallycloud.nio.codec.redis.future.RedisIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class TestRedisClient {

	public static void main(String[] args) throws Exception {
		
		SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(6379));

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
