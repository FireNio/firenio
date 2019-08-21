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
package sample.http11.proxy4cloud;

import java.io.Closeable;

import sample.http11.service.CountChannelListener;

import com.firenio.buffer.ByteBuf;
import com.firenio.common.Util;
import com.firenio.component.Channel;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.ChannelActiveListener;
import com.firenio.component.ChannelAliveListener;
import com.firenio.component.ChannelConnector;
import com.firenio.component.ChannelEventListenerAdapter;
import com.firenio.component.Frame;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoop;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.ProtocolCodec;

/**
 * @author wangkai
 */
public class NetDataTransferServer {

    private static final NetDataTransferServer instance = new NetDataTransferServer();
    private static final int                   MASK     = 0x12345678;

    public static NetDataTransferServer get() {
        return instance;
    }

    public static void main(String[] args) throws Exception {

        get().startup(new NioEventLoopGroup(true), 18088);

    }

    public synchronized void startup(NioEventLoopGroup group, int port) throws Exception {
        ChannelAcceptor context = new ChannelAcceptor(group, port);
        context.addProtocolCodec(new NetDataTransfer());
        context.addChannelIdleEventListener(new ChannelAliveListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new CountChannelListener());
        context.addChannelEventListener(CLOSE_TARGET);
        context.bind();
    }

    static class NetDataTransfer extends ProtocolCodec {

        @Override
        public Frame decode(Channel ch, ByteBuf src) throws Exception {
            Channel t = (Channel) ch.getAttachment();
            if (t == null) {
                short hostLen = src.getUnsignedByte(0);
                if (src.readableBytes() < hostLen + 5) {
                    ch.close();
                    return null;
                }
                byte b0 = src.getByte(1);
                byte b1 = src.getByte(2);
                if (!(b0 == 83 && b1 == 38)) {
                    ch.close();
                    return null;
                }
                byte[] hostBytes = new byte[hostLen];
                int    port      = src.getUnsignedShort(3);
                src.skipRead(5);
                src.readBytes(hostBytes);
                String           host    = new String(hostBytes);
                NioEventLoop     el      = ch.getEventLoop();
                ChannelConnector context = new ChannelConnector(el, host, port);
                context.addProtocolCodec(this);
                context.setPrintConfig(false);
                context.addChannelIdleEventListener(new ChannelActiveListener());
                context.addChannelEventListener(new LoggerChannelOpenListener());
                context.addChannelEventListener(new CountChannelListener());
                context.addChannelEventListener(CLOSE_TARGET);
                context.connect((raw_ch, ex) -> {
                    if (ex == null) {
                        raw_ch.setAttachment(ch);
                        ch.setAttachment(raw_ch);
                    } else {
                        Util.close(ch);
                    }
                }, 10000);
                return null;
            }
            ByteBuf buf = t.alloc().allocate(src.readableBytes());
            buf.writeBytes(src);
            t.writeAndFlush(buf);
            return null;
        }

        @Override
        public String getProtocolId() {
            return "NetDataTransfer";
        }

        @Override
        public int getHeaderLength() {
            return 0;
        }

    }

    static final ChannelEventListenerAdapter CLOSE_TARGET = new ChannelEventListenerAdapter() {
        @Override
        public void channelClosed(Channel ch) {
            Util.close((Closeable) ch.getAttachment());
        }
    };

}
