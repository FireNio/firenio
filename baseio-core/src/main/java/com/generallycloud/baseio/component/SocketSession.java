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

public interface SocketSession extends Session {

    public abstract boolean isEnableSSL();

    public abstract SSLEngine getSSLEngine();

    public abstract ProtocolDecoder getProtocolDecoder();

    public abstract ProtocolEncoder getProtocolEncoder();

    public abstract ProtocolFactory getProtocolFactory();

    @Override
    public abstract SocketChannelContext getContext();

    public abstract String getProtocolId();

    public abstract ExecutorEventLoop getExecutorEventLoop();

    /**
     * flush未encode的future
     * @param future
     */
    public abstract void flush(Future future);

    /**
     * flush已encode的future
     * @param future
     */
    public abstract void doFlush(ChannelFuture future);

    public abstract <T> T getOption(SocketOption<T> name) throws IOException;

    public abstract <T> void setOption(SocketOption<T> name, T value) throws IOException;

    public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

    public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

    public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

}
