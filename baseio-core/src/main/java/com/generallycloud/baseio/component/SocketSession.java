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
import java.util.Map;

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public interface SocketSession extends Closeable {

    void active();

    void clearAttributes();

    /**
     * flush未encode的future
     * @param future
     */
    void flush(Future future);

    /**
     * flush已encode的future
     * @param future
     */
    void flushChannelFuture(ChannelFuture future);
    
    Object getAttachment();

    Object getAttribute(Object key);

    Map<Object, Object> getAttributes();

    ByteBufAllocator getByteBufAllocator();

    SocketChannelContext getContext();

    long getCreationTime();

    Charset getEncoding();

    ExecutorEventLoop getExecutorEventLoop();

    long getLastAccessTime();

    String getLocalAddr();

    int getLocalPort();
    
    <T> T getOption(SocketOption<T> name) throws IOException;

    ProtocolCodec getProtocolCodec();

    String getProtocolId();

    String getRemoteAddr();

    String getRemoteAddrPort();
    
    int getRemotePort();

    int getSessionId();

    SSLEngine getSSLEngine();

    boolean inSelectorLoop();

    boolean isClosed();

    boolean isEnableSSL();

    boolean isOpened();

    Object removeAttribute(Object key);

    void setAttachment(Object attachment);

    void setAttribute(Object key, Object value);

    <T> void setOption(SocketOption<T> name, T value) throws IOException;

    void setProtocolCodec(ProtocolCodec protocolCodec);
    
    SocketChannel unsafe();

}
