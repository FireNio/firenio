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
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Frame;

/**
 * @author wangkai
 *
 */
public class ChannelAcceptor extends ChannelContext {

    private boolean             active = false;
    private Logger              logger = LoggerFactory.getLogger(getClass());
    private ServerSocketChannel selectableChannel;
    private ServerSocket        serverSocket;

    public ChannelAcceptor(NioEventLoopGroup group) {
        this(group, "0.0.0.0", 0);
    }

    public ChannelAcceptor(NioEventLoopGroup group, int port) {
        this(group, "0.0.0.0", port);
    }

    public ChannelAcceptor(NioEventLoopGroup group, String host, int port) {
        super(group, host, port);
        group.setAcceptor(true);
    }

    public ChannelAcceptor(String host, int port) {
        this(new NioEventLoopGroup(), host, port);
    }

    public ChannelAcceptor(int port) {
        this("0.0.0.0", port);
    }

    public void bind() throws IOException {
        bind(50);
    }

    public synchronized void bind(int backlog) throws IOException {
        if (active) {
            return;
        }
        LifeCycleUtil.start(getNioEventLoopGroup());
        LifeCycleUtil.start(this);
        this.selectableChannel = ServerSocketChannel.open();
        this.selectableChannel.configureBlocking(false);
        this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();
        this.getNioEventLoopGroup().registSelector(this);
        try {
            this.serverSocket.bind(getServerAddress(), 50);
        } catch (IOException e) {
            if ("Already bound".equalsIgnoreCase(e.getMessage()) || e instanceof BindException) {
                throw new BindException("Already bound at " + getPort());
            }
            throw e;
        }
        this.active = true;
        logger.info("server listening @" + getServerAddress());
    }

    public void broadcast(Frame frame) throws IOException {
        getChannelManager().broadcast(frame);
    }

    public void broadcast(ByteBuf buf) throws IOException {
        getChannelManager().broadcast(buf);
    }

    @Override
    public ServerSocketChannel getSelectableChannel() {
        return selectableChannel;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public synchronized void unbind() throws TimeoutException {
        active = false;
        CloseUtil.close(serverSocket);
        CloseUtil.close(selectableChannel);
        LifeCycleUtil.stop(this);
    }

}
