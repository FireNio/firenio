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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

import com.generallycloud.baseio.acceptor.ChannelAcceptor;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.connector.AbstractSocketChannelConnector;

/**
 * @author wangkai
 *
 */
public class NioSocketSelector implements SocketSelector {

    protected Selector                selector;
    protected ChannelBuilder          channelBuilder;
    protected NioSocketChannelContext context;

    NioSocketSelector(SelectorEventLoop selectorEventLoop, SelectableChannel channel,
            Selector selector) {
        this.selector = selector;
        this.context = selectorEventLoop.getChannelContext();
        this.channelBuilder = newChannelBuilder(selectorEventLoop, channel);
    }

    private ChannelBuilder newChannelBuilder(SelectorEventLoop selectorEventLoop,
            SelectableChannel channel) {
        NioSocketChannelContext context = selectorEventLoop.getChannelContext();
        if (context.getChannelService() instanceof ChannelAcceptor) {
            return new AcceptorChannelBuilder((ServerSocketChannel) channel,
                    selectorEventLoop.getEventLoopGroup());
        } else {
            return new ConnectorChannelBuilder(selectorEventLoop,
                    (java.nio.channels.SocketChannel) channel);
        }
    }

    @Override
    public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
        channelBuilder.buildChannel(eventLoop, k);
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }

    public void finishConnect(UnsafeSocketSession session, Throwable e) {
        ChannelService service = context.getChannelService();
        if (service instanceof AbstractSocketChannelConnector) {
            ((AbstractSocketChannelConnector) service).finishConnect(session, e);
        }
    }

    public java.nio.channels.Selector getSelector() {
        return selector;
    }

    protected NioSocketChannel regist(java.nio.channels.SocketChannel channel,
            SelectorEventLoop selectorLoop, int channelId) throws IOException {
        NioSocketSelector nioSelector = (NioSocketSelector) selectorLoop.getSelector();
        SelectionKey sk = channel.register(nioSelector.getSelector(), SelectionKey.OP_READ);
        // 绑定SocketChannel到SelectionKey
        NioSocketChannel socketChannel = (NioSocketChannel) sk.attachment();
        if (socketChannel != null) {
            return socketChannel;
        }
        socketChannel = new NioSocketChannel(selectorLoop, sk, channelId);
        sk.attach(socketChannel);
        // fire session open event
        socketChannel.fireOpend();
        return socketChannel;
    }

    @Override
    public int select() throws IOException {
        return selector.select();
    }

    @Override
    public int select(long timeout) throws IOException {
        return selector.select(timeout);
    }

    @Override
    public Set<SelectionKey> selectedKeys() throws IOException {
        return selector.selectedKeys();
    }

    @Override
    public int selectNow() throws IOException {
        return selector.selectNow();
    }

    @Override
    public void wakeup() {
        selector.wakeup();
    }

    interface ChannelBuilder {
        void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException;
    }

    class AcceptorChannelBuilder implements ChannelBuilder {

        private ServerSocketChannel    serverSocketChannel;
        private FixedAtomicInteger     channelIdGenerator;
        private SelectorEventLoopGroup selectorEventLoopGroup;

        public AcceptorChannelBuilder(ServerSocketChannel serverSocketChannel,
                SelectorEventLoopGroup selectorEventLoopGroup) {
            this.serverSocketChannel = serverSocketChannel;
            this.selectorEventLoopGroup = selectorEventLoopGroup;
            this.channelIdGenerator = selectorEventLoopGroup.getChannelContext().getCHANNEL_ID();
        }

        @Override
        public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
            final int channelId = channelIdGenerator.getAndIncrement();
            if (serverSocketChannel.getLocalAddress() == null) {
                return;
            }
            final java.nio.channels.SocketChannel channel = serverSocketChannel.accept();
            if (channel == null) {
                return;
            }
            SelectorEventLoop targetEventLoop = selectorEventLoopGroup.getNext();
            // 配置为非阻塞
            channel.configureBlocking(false);
            // 注册到selector，等待连接
            if (eventLoop == targetEventLoop) {
                regist(channel, targetEventLoop, channelId);
                return;
            }
            targetEventLoop.dispatch(new SelectorLoopEvent() {
                @Override
                public void fireEvent(SelectorEventLoop selectLoop) throws IOException {
                    regist(channel, selectLoop, channelId);
                }

                @Override
                public void close() throws IOException {}
            });
            targetEventLoop.wakeup();
        }
    }

    class ConnectorChannelBuilder implements ChannelBuilder {

        private java.nio.channels.SocketChannel jdkChannel;
        private SelectorEventLoop               selectorEventLoop;

        public ConnectorChannelBuilder(SelectorEventLoop selectorEventLoop,
                java.nio.channels.SocketChannel jdkChannel) {
            this.selectorEventLoop = selectorEventLoop;
            this.jdkChannel = jdkChannel;
        }

        @Override
        public void buildChannel(SelectorEventLoop eventLoop, SelectionKey k) throws IOException {
            try {
                if (!jdkChannel.finishConnect()) {
                    throw new IOException("connect failed");
                }
                SocketChannel channel = regist(jdkChannel, selectorEventLoop, 1);
                if (channel.isEnableSSL()) {
                    return;
                }
                finishConnect(channel.getSession(), null);
            } catch (IOException e) {
                finishConnect(null, e);
            }
        }
    }

}
