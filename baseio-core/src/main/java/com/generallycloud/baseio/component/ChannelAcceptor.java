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
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class ChannelAcceptor implements ChannelService {

    private boolean             active = false;
    private ChannelContext      context;
    private NioEventLoopGroup   group;
    private Logger              logger = LoggerFactory.getLogger(getClass());
    private ServerSocketChannel selectableChannel;
    private InetSocketAddress   serverAddress;
    private ServerSocket        serverSocket;
    private ChannelManager      channelManager;

    public ChannelAcceptor(ChannelContext context) {
        this(context, new NioEventLoopGroup());
    }

    public ChannelAcceptor(ChannelContext context, NioEventLoopGroup group) {
        Assert.notNull(context, "null context");
        Assert.notNull(group, "null group");
        this.context = context;
        this.group = group;
        this.group.setContext(context);
        this.context.setNioEventLoopGroup(group);
    }

    public synchronized void bind() throws IOException {
        if (active) {
            return;
        }
        Assert.notNull(context, "null context");
        Assert.notNull(group, "null group");
        this.group.setEnableSsl(context.isEnableSsl());
        LifeCycleUtil.stop(getContext());
        LifeCycleUtil.start(group);
        this.context.setChannelService(this);
        LifeCycleUtil.start(getContext());
        this.serverAddress = new InetSocketAddress(context.getPort());
        this.selectableChannel = ServerSocketChannel.open();
        this.selectableChannel.configureBlocking(false);
        this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();
        this.group.registSelector(context);
        this.channelManager = context.getChannelManager();
        try {
            this.serverSocket.bind(serverAddress, 50);
        } catch (IOException e) {
            if ("Already bound".equalsIgnoreCase(e.getMessage()) || e instanceof BindException) {
                throw new BindException("Already bound at " + context.getPort());
            }
            throw e;
        }
        this.active = true;
        LoggerUtil.prettyLog(logger, "server listening @{}", getServerSocketAddress());
    }

    public void broadcast(Future future) throws IOException {
        channelManager.broadcast(future);
    }

    public void broadcast(Future future, Collection<NioSocketChannel> channels) throws IOException {
        channelManager.broadcast(future, channels);
    }

    public void broadcastFuture(Future future) throws IOException {
        channelManager.broadcastFuture(future);
    }

    public void broadcastFuture(Future future, Collection<NioSocketChannel> channels) {
        channelManager.broadcastFuture(future, channels);
    }

    @Override
    public ChannelContext getContext() {
        return context;
    }

    @Override
    public ServerSocketChannel getSelectableChannel() {
        return selectableChannel;
    }

    @Override
    public InetSocketAddress getServerSocketAddress() {
        return serverAddress;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public synchronized void unbind() throws TimeoutException {
        active = false;
        CloseUtil.close(serverSocket);
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(group);
        LifeCycleUtil.stop(context);
    }

}
