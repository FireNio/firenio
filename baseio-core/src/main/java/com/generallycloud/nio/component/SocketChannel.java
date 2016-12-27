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

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.SslReadFuture;

public interface SocketChannel extends DuplexChannel, SelectorLoopEvent {

	public abstract void setWriteFuture(ChannelWriteFuture future);

	public abstract ChannelWriteFuture getWriteFuture();

	public abstract boolean isNetworkWeak();

	public abstract void upNetworkState();

	public abstract void downNetworkState();

	public abstract ChannelReadFuture getReadFuture();

	public abstract SslReadFuture getSslReadFuture();

	public abstract void setReadFuture(ChannelReadFuture future);

	public abstract void setSslReadFuture(SslReadFuture future);

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;

	public abstract void offer(ChannelWriteFuture future);

	public abstract boolean isBlocking();

	@Override
	public abstract SocketChannelContext getContext();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

	public abstract ProtocolFactory getProtocolFactory();

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

	public abstract int getWriteFutureSize();
	
	public abstract int getWriteFutureLength();

	public abstract boolean needFlush();

	@Override
	public abstract UnsafeSocketSession getSession();

	public abstract void fireEvent(SelectorLoopEvent event);
	
	public abstract EventLoop getEventLoop();
}
