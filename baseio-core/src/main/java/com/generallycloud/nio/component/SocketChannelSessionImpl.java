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
package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.ClosedChannelException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	private static final Logger		logger	= LoggerFactory.getLogger(SocketChannelSessionImpl.class);

	protected SocketChannel			channel;

	public SocketChannelSessionImpl(SocketChannel channel, Integer sessionID) {
		super(sessionID);
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
	public String getProtocolID() {
		return getChannel().getProtocolFactory().getProtocolID();
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

	private void exceptionCaught(IoEventHandle handle, ReadFuture future, Exception cause, IoEventState state) {
		try {
			handle.exceptionCaught(this, future, cause, state);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public boolean isBlocking() {
		return getChannel().isBlocking();
	}

	@Override
	public void flush(ReadFuture future) {

		if (future == null || future.flushed()) {
			return;
		}

		ChannelReadFuture crf = (ChannelReadFuture) future;
		
		SocketChannel socketChannel = getChannel();

		if (!socketChannel.isOpened()) {
			
			crf.flush();

			IoEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, new ClosedChannelException(toString()), IoEventState.WRITE);

			return;
		}

		try {

			ProtocolEncoder encoder = socketChannel.getProtocolEncoder();

			flush(encoder.encode(getByteBufAllocator(), crf.flush()));

		} catch (Exception e) {

			logger.debug(e.getMessage(), e);

			IoEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, e, IoEventState.WRITE);
		}
	}

	@Override
	public void flush(ChannelWriteFuture future) {

		try {

			channel.flush(future);

		} catch (Exception e) {

			ReleaseUtil.release(future);
			
			logger.debug(e.getMessage(), e);

			ReadFuture readFuture = future.getReadFuture();

			IoEventHandle handle = readFuture.getIOEventHandle();

			handle.exceptionCaught(this, readFuture, e, IoEventState.WRITE);
		}
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
