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
package com.generallycloud.baseio.codec.redis;

import java.io.IOException;

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.codec.redis.RedisFrame.RedisCommand;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.concurrent.Waiter;

//FIXME check null
public class RedisClient {

    private ChannelContext     context;
    private NioSocketChannel      ch;
    private RedisIOEventHandle ioEventHandle;
    private long               timeout;

    public RedisClient(NioSocketChannel ch) {
        this(ch, 3000);
    }

    public RedisClient(NioSocketChannel ch, long timeout) {
        this.timeout = timeout;
        this.ch = ch;
        this.context = ch.getContext();
        this.ioEventHandle = (RedisIOEventHandle) ch.getIoEventHandle();
    }

    private synchronized RedisNode sendCommand(byte[] command, byte[]... args) throws IOException {
        RedisFrame frame = new RedisCmdFrame();
        frame.writeCommand(command, args);
        Waiter waiter = ioEventHandle.newWaiter();
        ch.flush(frame);
        if (waiter.await(timeout)) {
            throw new TimeoutException("timeout");
        }
        return (RedisNode) waiter.getResponse();
    }

    private RedisNode sendCommand(RedisCommand command, byte[]... args) throws IOException {
        return sendCommand(command.raw, args);
    }

    public String set(String key, String value) throws IOException {
        byte[] _key = key.getBytes(context.getCharset());
        byte[] _value = value.getBytes(context.getCharset());
        RedisNode node = sendCommand(RedisCommand.SET, _key, _value);
        return (String) node.getValue();
    }

    public String get(String key) throws IOException {
        byte[] _key = key.getBytes(context.getCharset());
        RedisNode node = sendCommand(RedisCommand.GET, _key);
        return (String) node.getValue();
    }

    public String ping() throws IOException {
        RedisNode node = sendCommand(RedisCommand.PING);
        return (String) node.getValue();
    }

}
