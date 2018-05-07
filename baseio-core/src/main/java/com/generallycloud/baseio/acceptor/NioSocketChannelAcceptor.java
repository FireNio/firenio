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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SelectorEventLoopGroup;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class NioSocketChannelAcceptor extends AbstractChannelAcceptor {

    private Logger                  logger                 = LoggerFactory.getLogger(getClass());
    private ServerSocket            serverSocket           = null;
    private SelectableChannel       selectableChannel      = null;
    private SelectorEventLoopGroup  selectorEventLoopGroup = null;
    private NioSocketChannelContext context;

    NioSocketChannelAcceptor(NioSocketChannelContext context) {
        this.context = context;
    }

    @Override
    protected void bind(InetSocketAddress server) throws IOException {
        this.selectableChannel = ServerSocketChannel.open();
        this.selectableChannel.configureBlocking(false);
        this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();
        context.setSelectableChannel(selectableChannel);
        Configuration configuration = getContext().getConfiguration();
        String eventLoopName = "nio-process(tcp-" + server.getPort() + ")";
        int core_size = configuration.getSERVER_CORE_SIZE();
        this.selectorEventLoopGroup = new SelectorEventLoopGroup(context, eventLoopName, core_size);
        LifeCycleUtil.start(selectorEventLoopGroup);
        context.setSelectorEventLoopGroup(selectorEventLoopGroup);
        context.getSessionManager().init(context);
        try {
            this.serverSocket.bind(server, 50);
        } catch (IOException e) {
            if ("Already bound".equalsIgnoreCase(e.getMessage()) || e instanceof BindException) {
                int port = serverSocket.getLocalPort();
                if (port < 1) {
                    port = server.getPort();
                }
                throw new BindException("Already bound at " + port);
            }
            throw e;
        }
        LoggerUtil.prettyLog(logger, "server listening @{}", getServerSocketAddress());
    }

    @Override
    public NioSocketChannelContext getContext() {
        return context;
    }

    @Override
    protected void unbind0() {
        CloseUtil.close(serverSocket);
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(selectorEventLoopGroup);
    }

}
