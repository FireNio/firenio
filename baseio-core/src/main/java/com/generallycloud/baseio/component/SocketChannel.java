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
import java.net.SocketOption;
import java.nio.charset.Charset;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;
import com.generallycloud.baseio.protocol.SslFuture;

public interface SocketChannel extends Closeable {

    void active();

    void flushChannelFuture(ChannelFuture future);

    void finishHandshake(Exception e);

    void fireOpend();

    void flush(ChannelFuture future);

    ByteBufAllocator getByteBufAllocator();
    
    Charset getEncoding();

    int getChannelId();

    SocketChannelContext getContext();

    long getCreationTime();

    ExecutorEventLoop getExecutorEventLoop();

    long getLastAccessTime();

    String getLocalAddr();

    String getLocalHost();

    int getLocalPort();

    InetSocketAddress getLocalSocketAddress();

    <T> T getOption(SocketOption<T> name) throws IOException;

    ProtocolDecoder getProtocolDecoder();

    ProtocolEncoder getProtocolEncoder();

    ProtocolFactory getProtocolFactory();

    ChannelFuture getReadFuture();

    String getRemoteAddr();

    String getRemoteHost();

    int getRemotePort();

    InetSocketAddress getRemoteSocketAddress();

    UnsafeSocketSession getSession();

    SSLEngine getSSLEngine();

    SslHandler getSslHandler();

    SslFuture getSslReadFuture();

    int getWriteFutureLength();

    int getWriteFutureSize();

    boolean inSelectorLoop();

    boolean isBlocking();

    boolean isEnableSSL();

    boolean isOpened();

    <T> void setOption(SocketOption<T> name, T value) throws IOException;

    void setProtocolDecoder(ProtocolDecoder protocolDecoder);

    void setProtocolEncoder(ProtocolEncoder protocolEncoder);

    void setProtocolFactory(ProtocolFactory protocolFactory);

    void setReadFuture(ChannelFuture future);

    void setSslReadFuture(SslFuture future);

    void write(ByteBuf buf) throws IOException;

}
