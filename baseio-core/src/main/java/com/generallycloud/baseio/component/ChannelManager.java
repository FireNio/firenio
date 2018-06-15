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
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ChannelManager {

    private Map<Integer, NioSocketChannel> channels         = new ConcurrentHashMap<>();
    private Map<Integer, NioSocketChannel> readOnlyChannels = Collections.unmodifiableMap(channels);
    private ChannelContext                 context;

    public ChannelManager(ChannelContext context) {
        this.context = context;
    }

    public int getManagedChannelSize() {
        return channels.size();
    }

    public NioSocketChannel getChannel(Integer channelId) {
        return channels.get(channelId);
    }

    public void putChannel(NioSocketChannel channel) {
        channels.put(channel.getChannelId(), channel);
    }

    public void removeChannel(NioSocketChannel channel) {
        channels.remove(channel.getChannelId());
    }

    public void broadcast(Future future) throws IOException {
        broadcast(future, channels.values());
    }

    public void broadcastFuture(Future future) {
        broadcastFuture(future, channels.values());
    }

    public void broadcast(Future future, Collection<NioSocketChannel> channels) throws IOException {
        if (channels.size() == 0) {
            return;
        }
        NioSocketChannel channel = context.getSimulateSocketChannel();
        context.getProtocolCodec().encode(channel, future);
        broadcastFuture(future, channels);
    }

    public void broadcastFuture(Future future,
            Collection<NioSocketChannel> channels) {
        if (channels.size() == 0) {
            return;
        }
        for (NioSocketChannel ch : channels) {
            ch.flushFuture(future.duplicate());
        }
    }

    public Map<Integer, NioSocketChannel> getManagedChannels() {
        return readOnlyChannels;
    }

}
