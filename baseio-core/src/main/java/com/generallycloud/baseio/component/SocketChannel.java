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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.protocol.ChannelReadFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;
import com.generallycloud.baseio.protocol.SslReadFuture;

public interface SocketChannel extends DuplexChannel {

	public abstract ChannelReadFuture getReadFuture();

	public abstract SslReadFuture getSslReadFuture();

	public abstract void setReadFuture(ChannelReadFuture future);

	public abstract void setSslReadFuture(SslReadFuture future);

	public abstract void flush(ChannelWriteFuture future);

	public abstract void flush(ChannelReadFuture future);

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

	@Override
	public abstract UnsafeSocketSession getSession();

	public abstract ExecutorEventLoop getExecutorEventLoop();

	public abstract <T> T getOption(SocketOption<T> name) throws IOException;

	public abstract <T> void setOption(SocketOption<T> name, T value)
			throws IOException;

	public abstract boolean isEnableSSL();

	public abstract SSLEngine getSSLEngine();
	
	public abstract SslHandler getSslHandler();

	public abstract void finishHandshake(Exception e);

	public abstract void fireOpend();

	public abstract void write(ByteBuf buf) throws IOException;

}
