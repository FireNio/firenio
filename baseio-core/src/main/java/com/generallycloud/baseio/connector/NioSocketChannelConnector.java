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
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SelectorEventLoopGroup;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class NioSocketChannelConnector extends AbstractChannelConnector {

    private NioSocketChannelContext context;
    private SelectableChannel       selectableChannel = null;
    private SelectorEventLoopGroup  eventLoopGroup    = null;
    private Logger                  logger            = LoggerFactory.getLogger(getClass());

    NioSocketChannelConnector(NioSocketChannelContext context) {
        this.context = context;
    }

    @Override
    protected void close0() {
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(eventLoopGroup);
    }

    @Override
    protected void connect(InetSocketAddress server) throws IOException {
        LifeCycleUtil.stop(eventLoopGroup);
        selectableChannel = SocketChannel.open();
        selectableChannel.configureBlocking(false);
        getContext().setSelectableChannel(selectableChannel);
        String eventLoopName = "nio-process(tcp-" + server.getPort() + ")";
        eventLoopGroup = new SelectorEventLoopGroup(getContext(), eventLoopName, 1);
        LifeCycleUtil.start(eventLoopGroup);
        getContext().setSelectorEventLoopGroup(eventLoopGroup);
        getContext().getSessionManager().init(getContext());
        SocketChannel ch = (SocketChannel) selectableChannel;
        ch.connect(server);
        wait4connect();
    }

    @Override
    public NioSocketChannelContext getContext() {
        return context;
    }

    @Override
    protected void connected() {
        LoggerUtil.prettyLog(logger, "connected to server @{}", getServerSocketAddress());
    }

}
