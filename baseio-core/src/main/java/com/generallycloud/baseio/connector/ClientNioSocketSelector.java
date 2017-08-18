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
package com.generallycloud.baseio.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.baseio.component.NioSocketSelector;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketSelectorEventLoop;

/**
 * @author wangkai
 *
 */
public class ClientNioSocketSelector extends NioSocketSelector {

    private NioSocketChannelConnector       connector;
    private java.nio.channels.SocketChannel jdkChannel;

    public ClientNioSocketSelector(SocketSelectorEventLoop selectorEventLoop, Selector selector,
            SelectableChannel selectableChannel, NioSocketChannelConnector connector) {
        super(selectorEventLoop, selector);
        this.connector = connector;
        this.jdkChannel = (java.nio.channels.SocketChannel) selectableChannel;
    }

    @Override
    public void buildChannel(SelectionKey selectionKey) throws IOException {
        try {
            if (!jdkChannel.finishConnect()) {
                throw new IOException("connect failed");
            }
            SocketChannel channel = regist(jdkChannel, selectorEventLoop, 1);
            if (channel.isEnableSSL()) {
                return;
            }
            connector.finishConnect(channel.getSession(), null);
        } catch (IOException e) {
            connector.finishConnect(null, e);
        }
    }

}
