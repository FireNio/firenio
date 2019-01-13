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
package com.firenio.baseio.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.firenio.baseio.buffer.ByteBuf;

/**
 * @author wangkai
 *
 */
public class ChannelManager {

    private Map<Integer, Channel> channels         = new ConcurrentHashMap<>();
    private Map<Integer, Channel> readOnlyChannels = Collections.unmodifiableMap(channels);

    public void broadcast(ByteBuf buf) {
        broadcast(buf, channels.values());
    }

    public void broadcast(Frame frame) throws Exception {
        broadcast(frame, channels.values());
    }

    public Channel getChannel(Integer channelId) {
        return channels.get(channelId);
    }

    public Map<Integer, Channel> getManagedChannels() {
        return readOnlyChannels;
    }

    public int getManagedChannelSize() {
        return channels.size();
    }

    public void putChannel(Channel ch) {
        channels.put(ch.getChannelId(), ch);
    }

    public void removeChannel(Integer id) {
        channels.remove(id);
    }

    public static void broadcast(ByteBuf buf, Collection<Channel> chs) {
        if (chs.size() == 0) {
            buf.release();
            return;
        }
        try {
            for (Channel ch : chs) {
                ch.writeAndFlush(buf.duplicate());
            }
        } finally {
            buf.release();
        }
    }

    public static void broadcast(Frame frame, Collection<Channel> chs) throws Exception {
        if (chs.size() == 0) {
            return;
        }
        Channel ch = chs.iterator().next();
        if (ch != null) {
            broadcast(ch.encode(frame), chs);
        }
    }

}
