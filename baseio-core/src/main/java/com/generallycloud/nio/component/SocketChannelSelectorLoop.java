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
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.concurrent.LineEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {

	private Logger					logger			= LoggerFactory.getLogger(SocketChannelSelectorLoop.class);

	protected ByteBuf				buf				= null;

	protected ChannelByteBufReader	byteBufReader		= null;

	protected SocketChannelContext	context			= null;

	protected ProtocolDecoder		protocolDecoder	= null;

	protected ProtocolEncoder		protocolEncoder	= null;

	protected ProtocolFactory		protocolFactory	= null;

	protected EventLoop				eventLoop			= null;

	public SocketChannelSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {

		super(service, selectorLoops);

		this.context = (SocketChannelContext) service.getContext();

		this.eventLoop = context.getEventLoopGroup().getNext();

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.selectorLoops = selectorLoops;

		this.byteBufReader = context.getChannelByteBufReader();

		int readBuffer = context.getServerConfiguration().getSERVER_CHANNEL_READ_BUFFER();

		this.buf = UnpooledByteBufAllocator.getInstance().allocate(readBuffer);// FIXME
																	// 使用direct
	}

	@Override
	public void accept(SelectionKey selectionKey) {

		if (!selectionKey.isValid()) {
			cancelSelectionKey(selectionKey);
			return;
		}

		try {

			if (!selectionKey.isReadable()) {

				accept(selectionKey, selectionKey.channel());
				return;
			}

			accept((SocketChannel) selectionKey.attachment());

		} catch (Throwable e) {

			cancelSelectionKey(selectionKey, e);
		}

	}

	public void accept(SocketChannel channel) throws Exception {

		if (channel == null || !channel.isOpened()) {
			// 该channel已经被关闭
			return;
		}

		ByteBuf buf = this.buf;

		buf.clear();

		buf.nioBuffer();

		int length = buf.read(channel);

		if (length < 1) {

			if (length == -1) {
				CloseUtil.close(channel);
			}
			return;
		}

		channel.active();

		byteBufReader.accept(channel, buf.flip());
	}

	protected abstract void accept(SelectionKey selectionKey, SelectableChannel selectableChannel) throws IOException;

	@Override
	public SocketChannel buildSocketChannel(SelectionKey selectionKey) throws SocketException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (channel != null) {

			return channel;
		}

		channel = new NioSocketChannel(this, selectionKey);

		selectionKey.attach(channel);

		return channel;
	}

	@Override
	public void doStartup() throws IOException {

		if (eventLoop instanceof LineEventLoop) {
			((LineEventLoop) eventLoop).setMonitor(getMonitor());
		}

		super.doStartup();
	}

	@Override
	protected void doStop() {

		ReentrantLock lock = this.runLock;

		lock.lock();

		try {

			selectorLoopStrategy.stop();

		} finally {

			lock.unlock();
		}

		try {
			this.selector.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

		ReleaseUtil.release(buf);
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	public EventLoop getEventLoop() {
		return eventLoop;
	}

}
