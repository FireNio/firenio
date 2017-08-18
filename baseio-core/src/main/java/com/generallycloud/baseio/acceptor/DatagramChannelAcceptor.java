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
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.component.DatagramChannelContext;
import com.generallycloud.baseio.component.DatagramSelectorEventLoopGroup;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

public final class DatagramChannelAcceptor extends AbstractChannelAcceptor {

    private DatagramChannelContext         context                = null;

    private DatagramSocket                 datagramSocket         = null;

    private SelectableChannel              selectableChannel      = null;

    private DatagramSelectorEventLoopGroup selectorEventLoopGroup = null;

    public DatagramChannelAcceptor(DatagramChannelContext context) {
        this.context = context;
    }

    @Override
    protected void bind(InetSocketAddress socketAddress) throws IOException {
        initChannel();
        try {
            datagramSocket.bind(socketAddress);
        } catch (BindException e) {
            throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
        }
        initSelectorLoops();
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
    public void broadcast(final Future future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) {
        throw new UnsupportedOperationException();
    }

    @Override
    public DatagramChannelContext getContext() {
        return context;
    }

    @Override
    protected void destroyService() {
        CloseUtil.close(datagramSocket);
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(selectorEventLoopGroup);
    }

    private void initChannel() throws IOException {
        // 打开服务器套接字通道
        this.selectableChannel = DatagramChannel.open();
        // 服务器配置为非阻塞
        this.selectableChannel.configureBlocking(false);
        this.datagramSocket = ((DatagramChannel) this.selectableChannel).socket();
    }

    @Override
    public int getManagedSessionSize() {
        throw new UnsupportedOperationException();
    }

}
