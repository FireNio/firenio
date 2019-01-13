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
package test.io.load.http11;

import com.firenio.baseio.buffer.ByteBuf;
import com.firenio.baseio.codec.http11.HttpCodec;
import com.firenio.baseio.codec.http11.HttpFrame;
import com.firenio.baseio.codec.http11.HttpHeader;
import com.firenio.baseio.codec.http11.HttpStatic;
import com.firenio.baseio.codec.http11.WebSocketCodec;
import com.firenio.baseio.component.ChannelAcceptor;
import com.firenio.baseio.component.Frame;
import com.firenio.baseio.component.IoEventHandle;
import com.firenio.baseio.component.LoggerChannelOpenListener;
import com.firenio.baseio.component.NioEventLoopGroup;
import com.firenio.baseio.component.Channel;

public class TestHttpLoadServer {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                HttpFrame f = (HttpFrame) frame;
                f.setResponseHeader(HttpHeader.Connection, HttpStatic.keep_alive_bytes);
                f.setResponseHeader(HttpHeader.Content_Type, HttpStatic.text_plain_bytes);
                f.setContent(ch.allocate());
                f.write("Hello World", ch);
                ByteBuf buf = ch.encode(f);
                ch.writeAndFlush(buf);
                ch.release(f);
            }

        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryPoolCapacity(1024 * 64);
        group.setMemoryPoolUnit(512);
        group.setEventLoopSize(2);
        ChannelAcceptor context = new ChannelAcceptor(group, 8080);
        context.addProtocolCodec(new HttpCodec(8));
        context.addProtocolCodec(new WebSocketCodec());
        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());

        context.bind();
    }
}
