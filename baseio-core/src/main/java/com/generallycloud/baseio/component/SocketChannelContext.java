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

import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public interface SocketChannelContext extends ChannelContext {

    public abstract SocketSessionManager getSessionManager();

    public abstract IoEventHandleAdaptor getIoEventHandleAdaptor();

    public abstract ExecutorEventLoopGroup getExecutorEventLoopGroup();

    public abstract BeatFutureFactory getBeatFutureFactory();

    public abstract void setBeatFutureFactory(BeatFutureFactory beatFutureFactory);

    public abstract void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor);

    public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

    public abstract ProtocolFactory getProtocolFactory();

    public abstract ProtocolEncoder getProtocolEncoder();

    public abstract ProtocolDecoder getProtocolDecoder();

    public abstract SslContext getSslContext();

    public abstract void setSslContext(SslContext sslContext);

    public abstract ChannelByteBufReader getChannelByteBufReader();

    public abstract boolean isEnableSSL();

    public abstract SocketSessionFactory getSessionFactory();

    public abstract void setSocketSessionFactory(SocketSessionFactory sessionFactory);

    public abstract ForeFutureAcceptor getForeReadFutureAcceptor();

    public abstract SocketSessionEventListenerWrapper getSessionEventListenerLink();

    public abstract SocketSessionIdleEventListenerWrapper getSessionIdleEventListenerLink();

    public abstract void addSessionEventListener(SocketSessionEventListener listener);

    public abstract void addSessionIdleEventListener(SocketSessionIdleEventListener listener);

    public abstract void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup);

}
