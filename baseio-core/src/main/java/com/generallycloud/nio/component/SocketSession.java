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

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public interface SocketSession extends Session {

	public abstract boolean isEnableSSL();

	public abstract SSLEngine getSSLEngine();

	public abstract SslHandler getSslHandler();

	public abstract void finishHandshake(Exception e);

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract ProtocolFactory getProtocolFactory();
	
	public abstract void setAttachment(int index, Object attachment);
	
	//FIXME 使用继承方式呢
	public abstract Object getAttachment(int index);
	
	@Override
	public abstract SocketChannelContext getContext();
	
	public abstract String getProtocolID();

	public abstract boolean isBlocking();

	public abstract ExecutorEventLoop getExecutorEventLoop();
	
	public abstract void flush(ReadFuture future) ;
	
	public abstract void flush(ChannelWriteFuture future);

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

}
