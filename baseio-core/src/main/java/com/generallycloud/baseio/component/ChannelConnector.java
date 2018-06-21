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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.MessageFormatter;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class ChannelConnector implements ChannelService, Closeable {

    private ChannelContext    context;
    private NioEventLoop      eventLoop;
    private NioEventLoopGroup group;
    private Logger            logger  = LoggerFactory.getLogger(getClass());
    private SocketChannel     selectableChannel;
    private InetSocketAddress serverAddress;
    private NioSocketChannel  channel;
    private long              timeout = 3000;
    private Waiter            waiter;

    public ChannelConnector(ChannelContext context, NioEventLoop eventLoop) {
        Assert.notNull(context, "null context");
        Assert.notNull(eventLoop, "null eventLoop");
        this.context = context;
        this.eventLoop = eventLoop;
        this.group = eventLoop.getGroup();
        this.group.setContext(context);
        this.context.setNioEventLoopGroup(group);
    }

    public ChannelConnector(ChannelContext context) {
        this(context, new NioEventLoopGroup(1));
    }

    public ChannelConnector(ChannelContext context, NioEventLoopGroup group) {
        Assert.notNull(context, "null context");
        Assert.notNull(group, "null group");
        this.context = context;
        this.group = group;
        this.context.setNioEventLoopGroup(group);
        this.group.setContext(context);
        this.group.setEventLoopSize(1);
    }

    @Override
    public synchronized void close() throws IOException {
        if (getChannel() != null) {
            CloseUtil.close(getChannel());
        }
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(getContext());
        if (!group.isSharable()) {
            LifeCycleUtil.stop(group);
        }
    }

    public synchronized NioSocketChannel connect() throws IOException {
        if (isActive()) {
            return channel;
        }
        this.group.setEnableSsl(context.isEnableSsl());
        LifeCycleUtil.stop(getContext());
        LifeCycleUtil.start(group);
        String host = context.getHost();
        int port = context.getPort();
        this.channel = null;
        this.context.setChannelService(this);
        LifeCycleUtil.start(getContext());
        this.waiter = new Waiter();
        this.serverAddress = new InetSocketAddress(host, port);
        this.selectableChannel = SocketChannel.open();
        this.selectableChannel.configureBlocking(false);
        this.group.registSelector(context);
        SocketChannel ch = (SocketChannel) selectableChannel;
        ch.connect(serverAddress);
        wait4connect(timeout);
        return getChannel();

    }

    //FIXME protected
    public void finishConnect(NioSocketChannel channel, Throwable exception) {
        Waiter waiter = this.waiter;
        if (waiter == null) {
            CloseUtil.close(channel);
            return;
        }
        this.channel = channel;
        if (exception != null) {
            waiter.response(exception);
        } else {
            waiter.response(channel);
        }
        if (waiter.isTimeouted()) {
            CloseUtil.close(channel);
        }
    }

    @Override
    public ChannelContext getContext() {
        return context;
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public SocketChannel getSelectableChannel() {
        return selectableChannel;
    }

    @Override
    public InetSocketAddress getServerSocketAddress() {
        return serverAddress;
    }

    public NioSocketChannel getChannel() {
        return channel;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    public boolean isConnected() {
        return getChannel() != null && getChannel().isOpened();
    }

    public void setEventLoop(NioEventLoop eventLoop) {
        this.eventLoop = eventLoop;
        this.group = eventLoop.getGroup();
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private void wait4connect(long timeout) throws IOException {
        if (eventLoop != null && eventLoop.inEventLoop()) {
            throw new IOException("can not wait for connect in its event loop");
        }
        if (waiter.await(timeout)) {
            CloseUtil.close(this);
            throw new TimeoutException("connect to " + getServerSocketAddress() + " time out");
        }
        if (waiter.isFailed()) {
            CloseUtil.close(this);
            Throwable ex = (Throwable) waiter.getResponse();
            String errorMsg = MessageFormatter.format(
                    "connect to [{}] failed,nested exception is {}", getServerSocketAddress(),
                    ex.getMessage());
            throw new IOException(errorMsg, ex);
        }
        this.channel = (NioSocketChannel) waiter.getResponse();
        this.waiter = null;
        LoggerUtil.prettyLog(logger, "connected to server @{}", getServerSocketAddress());
    }

}
