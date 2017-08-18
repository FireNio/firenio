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
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.DatagramSelectorEventLoop;
import com.generallycloud.baseio.component.DatagramSelectorEventLoopGroup;
import com.generallycloud.baseio.component.DatagramSession;
import com.generallycloud.baseio.component.NioDatagramChannel;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.DatagramPacket;

public final class DatagramChannelConnector extends AbstractChannelConnector {

    private DatagramChannelContext         context                = null;
    private DatagramSession                session                = null;
    private SelectableChannel              selectableChannel      = null;
    private DatagramSelectorEventLoopGroup selectorEventLoopGroup = null;
    private Logger                         logger                 = LoggerFactory
            .getLogger(getClass());

    public DatagramChannelConnector(DatagramChannelContext context) {
        this.context = context;
    }

    @Override
    protected void destroyService() {
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(selectorEventLoopGroup);
    }

    @Override
    public synchronized DatagramSession connect() throws IOException {
        this.session = null;
        this.initialize();
        return getSession();
    }

    private void initSelectorLoops() {
        //FIXME socket selector event loop ?
        ServerConfiguration configuration = getContext().getServerConfiguration();
        int core_size = configuration.getSERVER_CORE_SIZE();
        this.selectorEventLoopGroup = new DatagramSelectorEventLoopGroup(getContext(), "io-process",
                core_size, (java.nio.channels.DatagramChannel) selectableChannel);
        LifeCycleUtil.start(selectorEventLoopGroup);
    }

    @Override
    protected void connect(InetSocketAddress socketAddress) throws IOException {
        this.initChannel();
        ((DatagramChannel) this.selectableChannel).connect(socketAddress);
        initSelectorLoops();
        DatagramSelectorEventLoop selectorLoop = selectorEventLoopGroup.getNext();
        @SuppressWarnings("resource")
        NioDatagramChannel channel = new NioDatagramChannel(selectorLoop,
                (DatagramChannel) selectableChannel, socketAddress, 1);
        this.session = channel.getSession();
        LoggerUtil.prettyLog(logger, "已连接到远程服务器 @{}", getServerSocketAddress());
    }

    @Override
    protected boolean canSafeClose() {
        return session == null || !session.inSelectorLoop();
    }

    @Override
    public DatagramChannelContext getContext() {
        return context;
    }

    @Override
    public DatagramSession getSession() {
        return session;
    }

    private void initChannel() throws IOException {
        this.selectableChannel = java.nio.channels.DatagramChannel.open();
        this.selectableChannel.configureBlocking(false);
    }

    public void sendDatagramPacket(DatagramPacket packet) throws IOException {
        session.sendPacket(packet);
    }

}
