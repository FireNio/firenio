/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.firenio.buffer.ByteBuf;

/**
 * @author wangkai
 */
public class ChannelManagerListener implements ChannelEventListener {

    private Map<Integer, Channel> channels         = new ConcurrentHashMap<>();
    private Map<Integer, Channel> readOnlyChannels = Collections.unmodifiableMap(channels);

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

    public void broadcast(ByteBuf buf) {
        broadcast(buf, channels.values());
    }

    public static void broadcast(Frame frame, Collection<Channel> chs) throws Exception {
        if (chs.size() == 0) {
            frame.release();
            return;
        }
        Channel ch = chs.iterator().next();
        if (ch == null) {
            frame.release();
            return;
        }
        try {
            ByteBuf buf = ch.getCodec().encode(ch, frame);
            broadcast(buf, chs);
        } finally {
            frame.release();
        }
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

    protected void putChannel(Channel ch) {
        channels.put(ch.getChannelId(), ch);
    }

    protected void removeChannel(Integer id) {
        channels.remove(id);
    }

    @Override
    public void channelClosed(Channel ch) {
        channels.remove(ch.getChannelId());
    }

    @Override
    public void channelOpened(Channel ch) throws Exception {
        channels.put(ch.getChannelId(), ch);
    }

}
