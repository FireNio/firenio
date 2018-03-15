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
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.charset.Charset;
import java.util.HashMap;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public class SocketSessionImpl implements SocketSession {

    protected Object                  attachment;
    protected HashMap<Object, Object> attributes = new HashMap<>();
    protected SocketChannel channel;

    public SocketSessionImpl(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void active() {
        getSocketChannel().active();
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }

    @Override
    public void close() {
        CloseUtil.close(getSocketChannel());
    }

    @Override
    public void doFlush(ChannelFuture future) {
        getSocketChannel().doFlush(future);
    }

    @Override
    public void flush(Future future) {
        getSocketChannel().flush((ChannelFuture) future);
    }

    @Override
    public Object getAttachment() {
        return attachment;
    }

    @Override
    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    @Override
    public HashMap<Object, Object> getAttributes() {
        return attributes;
    }

    @Override
    public ByteBufAllocator getByteBufAllocator() {
        return getSocketChannel().getByteBufAllocator();
    }

    protected SocketChannel getSocketChannel() {
        return channel;
    }

    @Override
    public SocketChannelContext getContext() {
        return getSocketChannel().getContext();
    }

    @Override
    public long getCreationTime() {
        return getSocketChannel().getCreationTime();
    }

    @Override
    public Charset getEncoding() {
        return getContext().getEncoding();
    }
    
    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return getSocketChannel().getExecutorEventLoop();
    }
    @Override
    public long getLastAccessTime() {
        return getSocketChannel().getLastAccessTime();
    }

    @Override
    public String getLocalAddr() {
        return getSocketChannel().getLocalAddr();
    }

    @Override
    public String getLocalHost() {
        return getSocketChannel().getLocalHost();
    }

    @Override
    public int getLocalPort() {
        return getSocketChannel().getLocalPort();
    }

    @Override
    public InetSocketAddress getLocalSocketAddress() {
        return getSocketChannel().getLocalSocketAddress();
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return getSocketChannel().getOption(name);
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return getSocketChannel().getProtocolDecoder();
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return getSocketChannel().getProtocolEncoder();
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return getSocketChannel().getProtocolFactory();
    }

    @Override
    public String getProtocolId() {
        return getSocketChannel().getProtocolFactory().getProtocolId();
    }

    @Override
    public String getRemoteAddr() {
        return getSocketChannel().getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return getSocketChannel().getRemoteHost();
    }

    @Override
    public int getRemotePort() {
        return getSocketChannel().getRemotePort();
    }

    @Override
    public InetSocketAddress getRemoteSocketAddress() {
        return getSocketChannel().getRemoteSocketAddress();
    }

    @Override
    public int getSessionId() {
        return getSocketChannel().getChannelId();
    }

    @Override
    public SSLEngine getSSLEngine() {
        return getSocketChannel().getSSLEngine();
    }

    @Override
    public boolean inSelectorLoop() {
        return getSocketChannel().inSelectorLoop();
    }

    @Override
    public boolean isClosed() {
        return !isOpened();
    }

    @Override
    public boolean isEnableSSL() {
        return getSocketChannel().isEnableSSL();
    }

    @Override
    public boolean isOpened() {
        return getSocketChannel().isOpened();
    }

    @Override
    public Object removeAttribute(Object key) {
        return attributes.remove(key);
    }

    @Override
    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        getSocketChannel().setOption(name, value);
    }

    @Override
    public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
        getSocketChannel().setProtocolDecoder(protocolDecoder);
    }

    @Override
    public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
        getSocketChannel().setProtocolEncoder(protocolEncoder);
    }

    @Override
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        getSocketChannel().setProtocolFactory(protocolFactory);
    }

    @Override
    public String toString() {
        return getSocketChannel().toString();
    }


}
