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
package com.generallycloud.test.io.redis;

import com.generallycloud.baseio.codec.redis.RedisClient;
import com.generallycloud.baseio.codec.redis.RedisCodec;
import com.generallycloud.baseio.codec.redis.RedisIOEventHandle;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.ChannelConnector;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioSocketChannel;

public class TestRedisClient {

    public static void main(String[] args) throws Exception {

        ChannelContext context = new ChannelContext(6379);

        ChannelConnector connector = new ChannelConnector(context);

        context.setIoEventHandle(new RedisIOEventHandle());

        context.addChannelEventListener(new LoggerChannelOpenListener());

        context.setProtocolCodec(new RedisCodec());

        NioSocketChannel channel = connector.connect();

        RedisClient client = new RedisClient(channel);

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
