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
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public class SocketSessionImpl implements SocketSession {

    protected Object                  attachment;
    protected HashMap<Object, Object> attributes = new HashMap<>();
    protected SocketChannel           channel;

    SocketSessionImpl(SocketChannel channel) {
        this.channel = channel;
    }

    @Override
    public void active() {
        unsafe().active();
    }

    @Override
    public void clearAttributes() {
        attributes.clear();
    }

    @Override
    public void close() {
        CloseUtil.close(unsafe());
    }

    @Override
    public void flush(Future future) {
        unsafe().flush((ChannelFuture) future);
    }

    @Override
    public void flushChannelFuture(ChannelFuture future) {
        unsafe().flushChannelFuture(future);
    }
    
    @Override
    public void flush(Collection<ChannelFuture> futures) {
        unsafe().flush(futures);
    }

    @Override
    public void flushChannelFuture(Collection<ChannelFuture> futures) {
        unsafe().flushChannelFuture(futures);
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
    public ByteBufAllocator allocator() {
        return unsafe().allocator();
    }

    @Override
    public SocketChannelContext getContext() {
        return unsafe().getContext();
    }

    @Override
    public long getCreationTime() {
        return unsafe().getCreationTime();
    }

    @Override
    public Charset getEncoding() {
        return unsafe().getEncoding();
    }

    @Override
    public ExecutorEventLoop getExecutorEventLoop() {
        return unsafe().getExecutorEventLoop();
    }

    @Override
    public long getLastAccessTime() {
        return unsafe().getLastAccessTime();
    }

    @Override
    public String getLocalAddr() {
        return unsafe().getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return unsafe().getLocalPort();
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return unsafe().getOption(name);
    }

    @Override
    public ProtocolCodec getProtocolCodec() {
        return unsafe().getProtocolCodec();
    }

    @Override
    public String getProtocolId() {
        return unsafe().getProtocolCodec().getProtocolId();
    }

    @Override
    public String getRemoteAddr() {
        return unsafe().getRemoteAddr();
    }

    @Override
    public String getRemoteAddrPort() {
        return unsafe().getRemoteAddrPort();
    }

    @Override
    public int getRemotePort() {
        return unsafe().getRemotePort();
    }

    @Override
    public int getSessionId() {
        return unsafe().getChannelId();
    }

    @Override
    public SocketChannel unsafe() {
        return channel;
    }

    @Override
    public SSLEngine getSSLEngine() {
        return unsafe().getSSLEngine();
    }

    @Override
    public boolean inSelectorLoop() {
        return unsafe().inSelectorLoop();
    }

    @Override
    public boolean isClosed() {
        return !isOpened();
    }

    @Override
    public boolean isEnableSsl() {
        return unsafe().isEnableSsl();
    }

    @Override
    public boolean isOpened() {
        return unsafe().isOpened();
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
        unsafe().setOption(name, value);
    }

    @Override
    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        unsafe().setProtocolCodec(protocolCodec);
    }

    @Override
    public String toString() {
        return unsafe().toString();
    }

}
