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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Set;

/**
 * @author wangkai
 *
 */
public abstract class NioSocketSelector implements SocketSelector {

    private Selector                  selector = null;

    protected SocketSelectorEventLoop selectorEventLoop;

    public NioSocketSelector(SocketSelectorEventLoop selectorEventLoop, Selector selector) {
        this.selectorEventLoop = selectorEventLoop;
        this.selector = selector;
    }

    @Override
    public int selectNow() throws IOException {
        return selector.selectNow();
    }

    @Override
    public int select(long timeout) throws IOException {
        return selector.select(timeout);
    }

    @Override
    public Set<SelectionKey> selectedKeys() throws IOException {
        return selector.selectedKeys();
    }

    public java.nio.channels.Selector getSelector() {
        return selector;
    }

    @Override
    public void close() throws IOException {
        selector.close();
    }

    @Override
    public void wakeup() {
        selector.wakeup();
    }

    protected NioSocketChannel regist(java.nio.channels.SocketChannel channel,
            SocketSelectorEventLoop selectorLoop, int channelId) throws IOException {
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

}
