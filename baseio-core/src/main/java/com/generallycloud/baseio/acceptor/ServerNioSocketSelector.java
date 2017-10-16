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
package com.generallycloud.baseio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.baseio.component.NioSocketSelector;
import com.generallycloud.baseio.component.SelectorLoopEventAdapter;
import com.generallycloud.baseio.component.SocketSelectorEventLoop;
import com.generallycloud.baseio.component.SocketSelectorEventLoopGroup;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;

/**
 * @author wangkai
 *
 */
public class ServerNioSocketSelector extends NioSocketSelector {

    private SocketSelectorEventLoopGroup selectorEventLoopGroup;

    private ServerSocketChannel          serverSocketChannel;

    private FixedAtomicInteger           channelIdGenerator;

    public ServerNioSocketSelector(SocketSelectorEventLoop loop, Selector selector,
            SelectableChannel channel) {
        super(loop, selector);
        this.selectorEventLoopGroup = loop.getEventLoopGroup();
        this.serverSocketChannel = (ServerSocketChannel) channel;
        this.channelIdGenerator = loop.getChannelContext().getCHANNEL_ID();
    }

    @Override
    public void buildChannel(SelectionKey k) throws IOException {

        final int channelId = channelIdGenerator.getAndIncrement();

        final java.nio.channels.SocketChannel channel = serverSocketChannel.accept();

        SocketSelectorEventLoop selectorLoop = selectorEventLoopGroup.getNext();

        // 配置为非阻塞
        channel.configureBlocking(false);

        // 注册到selector，等待连接
        if (selectorLoop.isMainEventLoop()) {
            regist(channel, selectorLoop, channelId);
            return;
        }

        selectorLoop.dispatch(new SelectorLoopEventAdapter() {
            @Override
            public void fireEvent(SocketSelectorEventLoop selectLoop) throws IOException {
                regist(channel, selectLoop, channelId);
            }
        });

        selectorLoop.wakeup();

    }

}
