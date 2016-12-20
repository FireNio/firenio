/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.nio.component;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.SocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.ssl.SslContext;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public interface SocketChannelContext extends ChannelContext {
	
	public abstract void setSessionManager(SocketSessionManager sessionManager) ;
	
	@Override
	public abstract SocketSessionManager getSessionManager();

	public abstract IoEventHandleAdaptor getIoEventHandleAdaptor();

	public abstract EventLoopGroup getEventLoopGroup();

	public abstract int getSessionAttachmentSize();
	
	public abstract void setSessionAttachmentSize(int sessionAttachmentSize);

	public abstract BeatFutureFactory getBeatFutureFactory();

	public abstract void setBeatFutureFactory(BeatFutureFactory beatFutureFactory);

	public abstract void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor);

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);
	
	public abstract ProtocolFactory getProtocolFactory();
	
	public abstract ProtocolEncoder getProtocolEncoder();
	
	public abstract SslContext getSslContext() ;

	public abstract void setSslContext(SslContext sslContext) ;
	
	public abstract ChannelByteBufReader getChannelByteBufReader();

	public abstract boolean isEnableSSL() ;
	
	public abstract SocketSessionFactory getSessionFactory() ;

	public abstract void setSocketSessionFactory(SocketSessionFactory sessionFactory) ;
	
	public abstract ForeReadFutureAcceptor getForeReadFutureAcceptor();
	
	public abstract Linkable<SocketSessionEventListener> getSessionEventListenerLink();
	
	public abstract void addSessionEventListener(SocketSessionEventListener listener);
	
	public abstract void offerSessionMEvent(SocketSessionManagerEvent event);

	public abstract void setEventLoopGroup(EventLoopGroup eventLoopGroup);

}