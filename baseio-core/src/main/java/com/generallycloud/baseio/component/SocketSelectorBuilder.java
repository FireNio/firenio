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

import com.generallycloud.baseio.acceptor.ChannelAcceptor;

/**
 * @author wangkai
 *
 */
public class SocketSelectorBuilder {

    public SocketSelector build(SocketSelectorEventLoop selectorLoop) throws IOException {
        SocketChannelContext context = selectorLoop.getChannelContext();
        NioChannelService nioChannelService = (NioChannelService) context.getChannelService();
        SelectableChannel channel = nioChannelService.getSelectableChannel();
        java.nio.channels.Selector selector = openSelector();
        if (nioChannelService instanceof ChannelAcceptor) {
            if (selectorLoop.isMainEventLoop()) {
                channel.register(selector, SelectionKey.OP_ACCEPT);
            }
        } else {
            channel.register(selector, SelectionKey.OP_CONNECT);
        }
        return new NioSocketSelector(selectorLoop, channel, selector);
    }

    //FIXME publicSelectedKeys
    private java.nio.channels.Selector openSelector() throws IOException {
        return java.nio.channels.Selector.open();
    }
}
