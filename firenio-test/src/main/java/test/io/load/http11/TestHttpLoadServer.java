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
package test.io.load.http11;

import com.firenio.codec.http11.HttpCodec;
import com.firenio.codec.http11.HttpConnection;
import com.firenio.codec.http11.HttpContentType;
import com.firenio.codec.http11.HttpFrame;
import com.firenio.codec.http11.WebSocketCodec;
import com.firenio.component.Channel;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;

public class TestHttpLoadServer {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandle = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                HttpFrame f = (HttpFrame) frame;
                f.setConnection(HttpConnection.KEEP_ALIVE);
                f.setContentType(HttpContentType.text_plain);
                f.setString("Hello World", ch);
                ch.writeAndFlush(f);
                ch.release(f);
            }

        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryCapacity(1024 * 1024 * 64);
        group.setMemoryUnit(512);
        group.setEventLoopSize(2);
        ChannelAcceptor context = new ChannelAcceptor(group, 8080);
        context.addProtocolCodec(new HttpCodec(8));
        context.addProtocolCodec(new WebSocketCodec());
        context.setIoEventHandle(eventHandle);
        context.addChannelEventListener(new LoggerChannelOpenListener());

        context.bind();
    }
}
