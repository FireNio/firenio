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
package com.generallycloud.test.io.load.http11;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http11.HttpHeader;
import com.generallycloud.baseio.codec.http11.HttpStatic;
import com.generallycloud.baseio.codec.http11.ServerHttpCodec;
import com.generallycloud.baseio.codec.http11.ServerHttpFrame;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.protocol.Frame;

public class TestHttpLoadServer {
    
    public static void main(String[] args) throws Exception {
        
        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(NioSocketChannel channel, Frame frame) throws Exception {
                ServerHttpFrame f = (ServerHttpFrame) frame;
                f.setResponseHeader(HttpHeader.Content_Type_Bytes, HttpStatic.plain_bytes);
                f.setResponseHeader(HttpHeader.Server_Bytes, null);
                frame.write("Hello World", channel);
                ByteBuf buf = channel.encode(frame);
                channel.flush(buf);
                f.release(channel.getEventLoop());
            }

        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(1024 * 64);
        group.setMemoryPoolUnit(512);
        ChannelContext context = new ChannelContext(8087);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.setProtocolCodec(new ServerHttpCodec(4));
        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());

        acceptor.bind();
    }
}
