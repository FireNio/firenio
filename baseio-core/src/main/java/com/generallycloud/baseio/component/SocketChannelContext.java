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

import java.nio.charset.Charset;

import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.buffer.ByteBufAllocatorManager;
import com.generallycloud.baseio.collection.Attributes;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public interface SocketChannelContext extends Attributes, LifeCycle {

    void addSessionEventListener(SocketSessionEventListener listener);

    void addSessionIdleEventListener(SocketSessionIdleEventListener listener);

    BeatFutureFactory getBeatFutureFactory();

    ByteBufAllocatorManager getByteBufAllocatorManager();

    ChannelByteBufReader newChannelByteBufReader();

    ChannelService getChannelService();

    Charset getEncoding();

    ExecutorEventLoopGroup getExecutorEventLoopGroup();

    ForeFutureAcceptor getForeReadFutureAcceptor();

    IoEventHandleAdaptor getIoEventHandleAdaptor();

    ProtocolDecoder getProtocolDecoder();

    ProtocolEncoder getProtocolEncoder();

    ProtocolFactory getProtocolFactory();

    ServerConfiguration getServerConfiguration();

    SocketSessionEventListenerWrapper getSessionEventListenerLink();

    SocketSessionFactory getSessionFactory();

    SocketSessionIdleEventListenerWrapper getSessionIdleEventListenerLink();

    long getSessionIdleTime();

    SocketSessionManager getSessionManager();

    SslContext getSslContext();

    long getStartupTime();

    boolean isEnableSSL();

    void setBeatFutureFactory(BeatFutureFactory beatFutureFactory);

    void setByteBufAllocatorManager(ByteBufAllocatorManager byteBufAllocatorManager);

    void setChannelService(ChannelService service);

    void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup);

    void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor);

    void setProtocolFactory(ProtocolFactory protocolFactory);

    void setSocketSessionFactory(SocketSessionFactory sessionFactory);

    void setSslContext(SslContext sslContext);

}
