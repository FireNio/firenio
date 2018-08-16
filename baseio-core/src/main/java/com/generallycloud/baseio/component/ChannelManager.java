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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ChannelManager {

    private Map<Integer, NioSocketChannel> channels         = new ConcurrentHashMap<>();
    private Map<Integer, NioSocketChannel> readOnlyChannels = Collections.unmodifiableMap(channels);

    public int getManagedChannelSize() {
        return channels.size();
    }

    public NioSocketChannel getChannel(Integer channelId) {
        return channels.get(channelId);
    }

    public void putChannel(NioSocketChannel ch) {
        channels.put(ch.getChannelId(), ch);
    }

    public void removeChannel(NioSocketChannel ch) {
        channels.remove(ch.getChannelId());
    }

    public void broadcast(Frame frame) throws IOException {
        broadcast(frame, channels.values());
    }

    public void broadcast(ByteBuf buf) {
        broadcast(buf, channels.values());
    }

    public static void broadcast(Frame frame, Collection<NioSocketChannel> chs)
            throws IOException {
        if (chs.size() == 0) {
            return;
        }
        NioSocketChannel ch = chs.iterator().next();
        if (ch != null) {
            broadcast(ch.encode(frame), chs);
        }
    }

    public static void broadcast(ByteBuf buf, Collection<NioSocketChannel> chs) {
        if (chs.size() == 0) {
            buf.release();
            return;
        }
        try {
            for (NioSocketChannel ch : chs) {
                ch.flush(buf.duplicate());
            }
        } finally {
            buf.release();
        }
    }

    public Map<Integer, NioSocketChannel> getManagedChannels() {
        return readOnlyChannels;
    }

}
