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
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

/**
 * @author wangkai
 *
 */
public class ChannelConnector extends ChannelContext implements Closeable {

    private NioEventLoop      eventLoop;
    private Logger            logger  = LoggerFactory.getLogger(getClass());
    private SocketChannel     javaChannel;
    private InetSocketAddress serverAddress;
    private NioSocketChannel  ch;
    private long              timeout = 3000;
    private Waiter            waiter;

    public ChannelConnector(NioEventLoopGroup group, String host, int port) {
        super(group, host, port);
        group.setAcceptor(false);
    }

    public ChannelConnector(NioEventLoop eventLoop, String host, int port) {
        this(eventLoop.getGroup(), host, port);
        this.eventLoop = eventLoop;
    }

    public ChannelConnector(String host, int port) {
        this(new NioEventLoopGroup(), host, port);
    }

    public ChannelConnector(int port) {
        this("127.0.0.1", port);
    }

    @Override
    public synchronized void close() throws IOException {
        if (getChannel() != null) {
            CloseUtil.close(getChannel());
        }
        CloseUtil.close(javaChannel);
        LifeCycleUtil.stop(this);
    }

    public synchronized NioSocketChannel connect() throws IOException {
        if (isConnected()) {
            return ch;
        }
        LifeCycleUtil.start(getNioEventLoopGroup());
        LifeCycleUtil.start(this);
        this.waiter = new Waiter();
        this.serverAddress = new InetSocketAddress(getHost(), getPort());
        this.javaChannel = SocketChannel.open();
        this.javaChannel.configureBlocking(false);
        this.getNioEventLoopGroup().registSelector(this);
        this.javaChannel.connect(serverAddress);
        this.wait4connect(timeout);
        return getChannel();

    }

    protected void finishConnect(NioSocketChannel ch, Throwable exception) {
        Waiter waiter = this.waiter;
        if (waiter == null) {
            CloseUtil.close(ch);
            return;
        }
        this.ch = ch;
        if (exception != null) {
            waiter.response(exception);
        } else {
            waiter.response(ch);
        }
        if (waiter.isTimeouted()) {
            CloseUtil.close(ch);
        }
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public SocketChannel getSelectableChannel() {
        return javaChannel;
    }

    @Override
    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public NioSocketChannel getChannel() {
        return ch;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    public boolean isConnected() {
        return ch != null && ch.isOpened();
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
            throw new TimeoutException("connect to " + getServerAddress() + " time out");
        }
        if (waiter.isFailed()) {
            CloseUtil.close(this);
            Throwable ex = (Throwable) waiter.getResponse();
            String errorMsg = "connect to " + getServerAddress() + " failed, nested exception is "
                    + ex.getMessage();
            throw new IOException(errorMsg, ex);
        }
        this.ch = (NioSocketChannel) waiter.getResponse();
        this.waiter = null;
        LoggerUtil.prettyLog(logger, "connected to server @" + getServerAddress());
    }

    @Override
    public String toString() {
        NioSocketChannel ch = this.ch;
        if (ch == null) {
            return super.toString();
        }
        return ch.toString();
    }

}
