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

import com.generallycloud.nio.DisconnectException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class SocketChannelSessionImpl extends SessionImpl implements SocketSession {

	private static final Logger		logger	= LoggerFactory.getLogger(SocketChannelSessionImpl.class);

	protected Waiter<Exception>		handshakeWaiter;
	protected SSLEngine			sslEngine;
	protected SslHandler			sslHandler;
	protected SocketChannel			channel;

	public SocketChannelSessionImpl(SocketChannel channel, Integer sessionID) {
		super(sessionID);
		this.channel = channel;
	}
	
	@Override
	public SocketChannelContext getContext() {
		return channel.getContext();
	}

	@Override
	protected Channel getChannel() {
		return channel;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return channel.getProtocolEncoder();
	}

	@Override
	public String getProtocolID() {
		return channel.getProtocolFactory().getProtocolID();
	}

	@Override
	public void finishHandshake(Exception e) {
		if (getContext().getSslContext().isClient()) {
			this.handshakeWaiter.setPayload(e);
		}
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
		return getContext().isEnableSSL();
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
		return channel.isBlocking();
	}

	@Override
	public void flush(ReadFuture future) {

		if (future == null || future.flushed()) {
			return;
		}

		ChannelReadFuture crf = (ChannelReadFuture) future;
		
		SocketChannel socketChannel = this.channel;

		if (!socketChannel.isOpened()) {
			
			crf.flush();

			IoEventHandle handle = future.getIOEventHandle();

			exceptionCaught(handle, future, new DisconnectException("disconnected"), IoEventState.WRITE);

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

			// FIXME 部分情况下可以不在业务线程做wrapssl
			if (isEnableSSL()) {
				future.wrapSSL(this, sslHandler);
			}

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
		return channel.getProtocolDecoder();
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return channel.getProtocolFactory();
	}

	@Override
	public SSLEngine getSSLEngine() {
		return sslEngine;
	}

	@Override
	public ExecutorEventLoop getExecutorEventLoop() {
		return channel.getExecutorEventLoop();
	}

	@Override
	public SslHandler getSslHandler() {
		return getContext().getSslContext().getSslHandler();
	}

	@Override
	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		channel.setProtocolDecoder(protocolDecoder);
	}

	@Override
	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		channel.setProtocolEncoder(protocolEncoder);
	}

	@Override
	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		channel.setProtocolFactory(protocolFactory);
	}

}
