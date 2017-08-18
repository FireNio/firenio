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
import java.net.SocketOption;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public abstract class SocketChannelSessionImpl extends AbstractSession implements SocketSession {

    protected SocketChannel channel;

    public SocketChannelSessionImpl(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public SocketChannelContext getContext() {
        return getChannel().getContext();
    }

    @Override
    protected SocketChannel getChannel() {
        return channel;
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return getChannel().getProtocolEncoder();
    }

    @Override
    public String getProtocolId() {
        return getChannel().getProtocolFactory().getProtocolId();
    }

    @Override
    public boolean isEnableSSL() {
        return getChannel().isEnableSSL();
    }

    @Override
    public void flush(Future future) {
        getChannel().flush((ChannelFuture) future);
    }

    @Override
    public void doFlush(ChannelFuture future) {
        getChannel().doFlush(future);
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return getChannel().getProtocolDecoder();
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return getChannel().getProtocolFactory();
    }

    @Override
    public SSLEngine getSSLEngine() {
        return getChannel().getSSLEngine();
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return getChannel().getExecutorEventLoop();
    }

    @Override
    public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
        getChannel().setProtocolDecoder(protocolDecoder);
    }

    @Override
    public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
        getChannel().setProtocolEncoder(protocolEncoder);
    }

    @Override
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        getChannel().setProtocolFactory(protocolFactory);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return getChannel().getOption(name);
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        getChannel().setOption(name, value);
    }

}
