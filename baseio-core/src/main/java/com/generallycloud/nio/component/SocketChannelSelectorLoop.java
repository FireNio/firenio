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
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.concurrent.ExecutorEventLoop;
import com.generallycloud.nio.component.concurrent.LineEventLoop;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;

public abstract class SocketChannelSelectorLoop extends AbstractSelectorLoop {

	protected ByteBuf				buf				= null;

	protected ChannelByteBufReader	byteBufReader		= null;

	protected SocketChannelContext	context			= null;

	protected ProtocolDecoder		protocolDecoder	= null;

	protected ProtocolEncoder		protocolEncoder	= null;

	protected ProtocolFactory		protocolFactory	= null;

	protected ExecutorEventLoop		executorEventLoop	= null;

	public SocketChannelSelectorLoop(ChannelService service, SelectorEventLoopGroup group) {

		super(service, group);

		this.context = (SocketChannelContext) service.getContext();

		this.executorEventLoop = context.getExecutorEventLoopGroup().getNext();

		this.protocolFactory = context.getProtocolFactory();

		this.protocolDecoder = protocolFactory.getProtocolDecoder();

		this.protocolEncoder = protocolFactory.getProtocolEncoder();

		this.byteBufReader = context.getChannelByteBufReader();

		int readBuffer = context.getServerConfiguration().getSERVER_CHANNEL_READ_BUFFER();

		// FIXME 使用direct
		this.buf = UnpooledByteBufAllocator.getInstance().allocate(readBuffer);
	}

	@Override
	public void accept(SocketChannel channel) {

		if (!channel.isOpened()) {
			return;
		}

		try {

			accept0(channel);

		} catch (Throwable e) {

			cancelSelectionKey(channel, e);
		}

	}

	public void accept0(SocketChannel channel) throws Exception {

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

	@Override
	public void doStartup() throws IOException {

		if (executorEventLoop instanceof LineEventLoop) {
			((LineEventLoop) executorEventLoop).setMonitor(this);
		}

		super.doStartup();
	}

	@Override
	protected void doStop() {

		ReentrantLock lock = this.runLock;

		lock.lock();

		try {

			List<SelectorLoopEvent> eventBuffer = positiveEvents.getBuffer();

			for (SelectorLoopEvent event : eventBuffer) {

				CloseUtil.close(event);
			}

		} finally {

			lock.unlock();
		}

		CloseUtil.close(selector);

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

	public ExecutorEventLoop getExecutorEventLoop() {
		return executorEventLoop;
	}


}
