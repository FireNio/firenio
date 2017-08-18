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
import com.generallycloud.baseio.component.NioChannelService;
import com.generallycloud.baseio.component.NioGlobalSocketSessionManager;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketSelectorBuilder;
import com.generallycloud.baseio.component.SocketSelectorEventLoopGroup;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class NioSocketChannelConnector extends AbstractSocketChannelConnector
        implements NioChannelService {

    private NioSocketChannelContext      context;
    private SelectableChannel            selectableChannel      = null;
    private SocketSelectorBuilder        selectorBuilder        = null;
    private SocketSelectorEventLoopGroup selectorEventLoopGroup = null;
    private Logger                       logger                 = LoggerFactory
            .getLogger(getClass());

    //FIXME 优化
    protected NioSocketChannelConnector(NioSocketChannelContext context) {
        this.selectorBuilder = new ClientNioSocketSelectorBuilder(this);
        this.context = context;
    }

    @Override
    protected void destroyService() {
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(selectorEventLoopGroup);
    }

    private void initSelectorLoops() {
        //FIXME socket selector event loop ?
        ServerConfiguration configuration = getContext().getServerConfiguration();
        int core_size = configuration.getSERVER_CORE_SIZE();
        this.selectorEventLoopGroup = new SocketSelectorEventLoopGroup(getContext(), "io-process",
                core_size);
        LifeCycleUtil.start(selectorEventLoopGroup);
    }

    @Override
    protected void connect(InetSocketAddress socketAddress) throws IOException {
        LifeCycleUtil.stop(selectorEventLoopGroup);
        initChannel();
        initSelectorLoops();
        initNioSessionMananger();
        ((SocketChannel) this.selectableChannel).connect(socketAddress);
        wait4connect();
    }

    private void initNioSessionMananger() {
        NioGlobalSocketSessionManager manager = getContext().getSessionManager();
        manager.init(getContext());
    }

    @Override
    public NioSocketChannelContext getContext() {
        return context;
    }

    private void initChannel() throws IOException {
        this.selectableChannel = SocketChannel.open();
        this.selectableChannel.configureBlocking(false);
    }

    @Override
    public SocketSelectorBuilder getSelectorBuilder() {
        return selectorBuilder;
    }

    @Override
    public SelectableChannel getSelectableChannel() {
        return selectableChannel;
    }

    @Override
    Logger getLogger() {
        return logger;
    }

    /**
     * @return the selectorEventLoopGroup
     */
    @Override
    public SocketSelectorEventLoopGroup getSelectorEventLoopGroup() {
        return selectorEventLoopGroup;
    }

}
