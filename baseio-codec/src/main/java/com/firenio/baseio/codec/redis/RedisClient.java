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
package com.firenio.baseio.codec.redis;

import com.firenio.baseio.TimeoutException;
import com.firenio.baseio.codec.redis.RedisCodec.RedisCommand;
import com.firenio.baseio.component.ChannelContext;
import com.firenio.baseio.component.Channel;
import com.firenio.baseio.concurrent.Waiter;

//FIXME check null
public class RedisClient {

    private Channel            ch;
    private ChannelContext     context;
    private RedisIOEventHandle ioEventHandle;
    private long               timeout;

    public RedisClient(Channel ch) {
        this(ch, 3000);
    }

    public RedisClient(Channel ch, long timeout) {
        this.timeout = timeout;
        this.ch = ch;
        this.context = ch.getContext();
        this.ioEventHandle = (RedisIOEventHandle) ch.getIoEventHandle();
    }

    public String get(String key) throws Exception {
        byte[] _key = key.getBytes(context.getCharset());
        RedisNode node = sendCommand(RedisCommand.GET, _key);
        return (String) node.getValue();
    }

    public String ping() throws Exception {
        RedisNode node = sendCommand(RedisCommand.PING);
        return (String) node.getValue();
    }

    private synchronized RedisNode sendCommand(byte[] command, byte[]... args) throws Exception {
        RedisCmdFrame frame = new RedisCmdFrame();
        frame.writeCommand(command, args);
        Waiter<RedisCmdFrame> waiter = ioEventHandle.newWaiter();
        ch.writeAndFlush(frame);
        if (waiter.await(timeout)) {
            throw new TimeoutException("timeout");
        }
        return waiter.getResponse().getRedisNode();
    }

    private RedisNode sendCommand(RedisCommand command, byte[]... args) throws Exception {
        return sendCommand(command.raw, args);
    }

    public String set(String key, String value) throws Exception {
        byte[] _key = key.getBytes(context.getCharset());
        byte[] _value = value.getBytes(context.getCharset());
        RedisNode node = sendCommand(RedisCommand.SET, _key, _value);
        return (String) node.getValue();
    }

}
