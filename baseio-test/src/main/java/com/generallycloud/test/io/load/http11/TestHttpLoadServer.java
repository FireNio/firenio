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

import com.generallycloud.baseio.codec.http11.ServerHttpCodec;
import com.generallycloud.baseio.codec.http11.ServerHttpFuture;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.protocol.Future;

public class TestHttpLoadServer {

    public static void main(String[] args) throws Exception {

        IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

            @Override
            public void accept(NioSocketChannel channel, Future future) throws Exception {
                System.out.println("channel >>>>"+channel);
                future.write("hello world!8080", channel.getContext());
                ServerHttpFuture f = (ServerHttpFuture) future;
                channel.flush(future);
            }

        };

        NioEventLoopGroup group = new NioEventLoopGroup(8);
        group.setMemoryPoolCapacity(1024 * 1024 * 2 / 8);
        group.setMemoryPoolUnit(256);
        group.setEnableMemoryPoolDirect(true);
        group.setEnableMemoryPool(true);
        ChannelContext context = new ChannelContext(new Configuration(8080));
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.setProtocolCodec(new ServerHttpCodec());
        context.setIoEventHandle(eventHandleAdaptor);
        context.addChannelEventListener(new LoggerChannelOpenListener());

        acceptor.bind();
    }
}
