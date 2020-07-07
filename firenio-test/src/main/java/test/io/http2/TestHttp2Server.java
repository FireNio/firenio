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
package test.io.http2;

import com.firenio.codec.http2.Http2Codec;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.Frame;
import com.firenio.component.IoEventHandle;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.Channel;

public class TestHttp2Server {

    public static void main(String[] args) throws Exception {

        IoEventHandle eventHandleAdaptor = new IoEventHandle() {

            @Override
            public void accept(Channel ch, Frame frame) throws Exception {
                frame.write("Hello World", ch);
                ch.writeAndFlush(frame);
            }

        };

        NioEventLoopGroup group = new NioEventLoopGroup();
        group.setMemoryCapacity(1024 * 1024 * 4);
        group.setMemoryUnit(512);
        group.setEnableMemoryPool(true);
        ChannelAcceptor context = new ChannelAcceptor(group, 443);
        context.addProtocolCodec(new Http2Codec());
        context.setIoEventHandle(eventHandleAdaptor);
//        context.setApplicationProtocols(new String[]{"h2", "http/1.1"});
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.bind();
    }
}
