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

import javax.net.ssl.SSLEngine;

import com.generallycloud.baseio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.protocol.ChannelReadFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;
import com.generallycloud.baseio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	protected SocketChannel			channel;
	protected Object[]				attachments;

	public SocketChannelSessionImpl(SocketChannel channel) {
		this.channel = channel;
	}
	
	@Override
	public SocketChannelContext getContext() {
		return getChannel().getContext();
	}

	@Override
	protected SocketChannel getChannel() {
		return channel;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return getChannel().getProtocolEncoder();
	}

	@Override
	public String getProtocolId() {
		return getChannel().getProtocolFactory().getProtocolId();
	}
	
	@Override
	public void setAttachment(int index, Object attachment) {
		if (attachments == null) {
			attachments = new Object[getContext().getSessionAttachmentSize()];
		}
		this.attachments[index] = attachment;
	}
	
	@Override
	public Object getAttachment(int index) {
		if (attachments == null) {
			return null;
		}
		return attachments[index];
	}

	@Override
	public boolean isEnableSSL() {
		return getChannel().isEnableSSL();
	}

	@Override
	public void flush(ReadFuture future) {
		getChannel().flush((ChannelReadFuture)future);
	}

	@Override
	public void flush(ChannelWriteFuture future) {
		getChannel().flush(future);
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return getChannel().getProtocolDecoder();
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return getChannel().getProtocolFactory();
	}

	@Override
	public SSLEngine getSSLEngine() {
		return getChannel().getSSLEngine();
	}

	@Override
	public ExecutorEventLoop getExecutorEventLoop() {
		return getChannel().getExecutorEventLoop();
	}

	@Override
	public SslHandler getSslHandler() {
		return getContext().getSslContext().getSslHandler();
	}

	@Override
	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		getChannel().setProtocolDecoder(protocolDecoder);
	}

	@Override
	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		getChannel().setProtocolEncoder(protocolEncoder);
	}

	@Override
	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		getChannel().setProtocolFactory(protocolFactory);
	}

}
