/*
 * Copyright 2015 The Baseio Project
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
package test.io.redis;

import com.firenio.baseio.codec.redis.RedisClient;
import com.firenio.baseio.codec.redis.RedisCodec;
import com.firenio.baseio.codec.redis.RedisIOEventHandle;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.component.ChannelConnector;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.Channel;

public class TestRedisClient {

    public static void main(String[] args) throws Exception {

        ChannelConnector context = new ChannelConnector(6379);
        context.setIoEventHandle(new RedisIOEventHandle());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addProtocolCodec(new RedisCodec());
        Channel ch = context.connect();
        RedisClient client = new RedisClient(ch);
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
        Util.sleep(100);
        Util.close(context);

    }
}
