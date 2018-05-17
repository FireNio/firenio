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
import java.net.SocketOption;
import java.nio.charset.Charset;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

public interface SocketChannel extends Closeable {

    void active();

    void finishHandshake(Exception e);

    void fireOpend();

    void flush(ChannelFuture future);

    void flushChannelFuture(ChannelFuture future);

    ByteBufAllocator getByteBufAllocator();
    
    int getChannelId();
    
    SocketChannelContext getContext();

    long getCreationTime();

    Charset getEncoding();

    ExecutorEventLoop getExecutorEventLoop();

    long getLastAccessTime();
    
    ChannelThreadContext getChannelThreadContext();

    String getLocalAddr();

    int getLocalPort();

    <T> T getOption(SocketOption<T> name) throws IOException;

    ProtocolCodec getProtocolCodec();

    ChannelFuture getReadFuture();

    String getRemoteAddr();

    String getRemoteAddrPort();

    int getRemotePort();

    UnsafeSocketSession getSession();

    SSLEngine getSSLEngine();

    SslHandler getSslHandler();

    SslFuture getSslReadFuture();

    int getWriteFutureSize();

    boolean inSelectorLoop();

    boolean isBlocking();

    boolean isEnableSsl();

    boolean isOpened();

    <T> void setOption(SocketOption<T> name, T value) throws IOException;

    void setProtocolCodec(ProtocolCodec protocolCodec);

    void setReadFuture(ChannelFuture future);

    void setSslReadFuture(SslFuture future);

}
