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
import java.nio.channels.SocketChannel;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.Assert;
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

    private Callback         callback;
    private NioSocketChannel ch;
    private NioEventLoop     eventLoop;
    private SocketChannel    javaChannel;
    private Logger           logger  = LoggerFactory.getLogger(getClass());
    private long             timeout = 3000;
    private Waiter           waiter;

    public ChannelConnector(int port) {
        this("127.0.0.1", port);
    }

    public ChannelConnector(NioEventLoop eventLoop, String host, int port) {
        this(eventLoop.getGroup(), host, port);
        this.eventLoop = eventLoop;
    }

    public ChannelConnector(NioEventLoopGroup group) {
        this(group, "127.0.0.1", 0);
    }

    public ChannelConnector(NioEventLoopGroup group, String host, int port) {
        super(group, host, port);
        group.setAcceptor(false);
    }

    public ChannelConnector(String host, int port) {
        this(new NioEventLoopGroup(), host, port);
    }

    @Override
    public synchronized void close() throws IOException {
        CloseUtil.close(ch);
        CloseUtil.close(javaChannel);
        LifeCycleUtil.stop(this);
        if (!getNioEventLoopGroup().isSharable()) {
            this.eventLoop = null;
        }
        this.ch = null;
    }

    public synchronized NioSocketChannel connect() throws IOException {
        if (isConnected()) {
            return ch;
        }
        LifeCycleUtil.start(getNioEventLoopGroup());
        LifeCycleUtil.start(this);
        getNioEventLoopGroup().setContext(this);
        if (eventLoop == null) {
            eventLoop = getNioEventLoopGroup().getNext();
        }
        if (eventLoop.inEventLoop() && callback == null) {
            throw new IOException("connect in event loop but no callback found!");
        }
        this.waiter = new Waiter();
        this.javaChannel = SocketChannel.open();
        this.javaChannel.configureBlocking(false);
        this.eventLoop.registSelector(this);
        this.javaChannel.connect(getServerAddress());
        this.wait4connect(timeout);
        return getChannel();
    }

    public synchronized void connect(Callback callback) throws IOException {
        Assert.notNull(callback, "null callback");
        this.callback = callback;
        if (isConnected()) {
            callback.call(ch, null);
        } else {
            connect();
        }
    }

    protected void finishConnect(NioSocketChannel ch, Throwable exception) {
        this.ch = ch;
        if (callback != null) {
            callback.call(ch, exception);
            return;
        }
        if (exception != null) {
            waiter.response(exception);
        } else {
            waiter.response(ch);
        }
        if (waiter.isTimeouted()) {
            CloseUtil.close(ch);
        }
    }
    
    public NioSocketChannel getChannel() {
        return ch;
    }

    public NioEventLoop getEventLoop() {
        return eventLoop;
    }

    @Override
    public SocketChannel getSelectableChannel() {
        return javaChannel;
    }

    public long getTimeout() {
        return timeout;
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    public boolean isConnected() {
        NioSocketChannel ch = this.ch;
        return ch != null && ch.isOpened();
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        NioSocketChannel ch = this.ch;
        if (ch == null) {
            return super.toString();
        }
        return ch.toString();
    }

    private void wait4connect(long timeout) throws IOException {
        if (callback != null) {
            return;
        }
        if (eventLoop.inEventLoop()) {
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

    public interface Callback {

        void call(NioSocketChannel ch, Throwable ex);

    }

}
