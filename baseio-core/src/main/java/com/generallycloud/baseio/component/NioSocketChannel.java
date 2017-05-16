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
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SelectorEventLoop.SelectorLoopEvent;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;

public class NioSocketChannel extends AbstractSocketChannel implements SelectorLoopEvent {

	private SocketChannel				channel;
	private SelectionKey				selectionKey;
	private boolean					networkWeak;
	private NioSocketChannelContext		context;
	private SocketSelectorEventLoop		selectorEventLoop;
	private long						next_network_weak	= Long.MAX_VALUE;
	private volatile boolean			closing;

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public NioSocketChannel(SocketSelectorEventLoop selectorLoop, SelectionKey selectionKey) {
		super(selectorLoop);
		this.selectorEventLoop = selectorLoop;
		this.context = selectorLoop.getChannelContext();
		this.selectionKey = selectionKey;
		this.channel = (SocketChannel) selectionKey.channel();
	}

	@Override
	public NioSocketChannelContext getContext() {
		return context;
	}

	@Override
	public boolean isComplete() {
		return write_future == null && write_futures.size() == 0;
	}

	@Override
	public <T> T getOption(SocketOption<T> name) throws IOException {
		return channel.getOption(name);
	}

	@Override
	public <T> void setOption(SocketOption<T> name, T value) throws IOException {
		channel.setOption(name, value);
	}

	@Override
	protected InetSocketAddress getRemoteSocketAddress0() throws IOException {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	@Override
	protected InetSocketAddress getLocalSocketAddress0() throws IOException {
		return (InetSocketAddress) channel.getLocalAddress();
	}

	@Override
	protected void doFlush(ChannelWriteFuture future) {
		selectorEventLoop.dispatch(this);
	}

	@Override
	public void fireEvent(SocketSelectorEventLoop selectorLoop) throws IOException {

		if (!isOpened()) {
			throw new ClosedChannelException("closed");
		}
		
		ChannelWriteFuture future = this.write_future;

		if (future != null) {

			future.write(this);

			if (!future.isCompleted()) {
				return;
			}

			writeFutureLength.getAndAdd(-future.getBinaryLength());

			future.onSuccess(session);

			write_future = null;
			
			return;
			
		}
		
		future = write_futures.poll();
		
		if (future == null) {
			return;
		}
		// 如果这里写入失败会导致内存溢出，需要try
		try {
			future.write(this);
		} catch (Throwable e) {
			ReleaseUtil.release(future);
			throw e;
		}

		if (!future.isCompleted()) {
			this.write_future = future;
			return;
		}

		writeFutureLength.getAndAdd(-future.getBinaryLength());

		future.onSuccess(session);

	}
	
	private boolean isClosing() {
		return closing;
	}

	@Override
	public void close() throws IOException {
		
		synchronized (getCloseLock()) {
			
			if (!isOpened()) {
				releaseFutures();
				return;
			}

			if (inSelectorLoop()) {
				physicalClose();
				return;
			}

			if (isClosing()) {
				return;
			}

			closing = true;

			fireClose();
		}

	}

	private void fireClose() {

		final NioSocketChannel _channel = this;

		fireEvent(new SelectorLoopEventAdapter() {

			@Override
			public void fireEvent(SocketSelectorEventLoop selectLoop) throws IOException {
				CloseUtil.close(_channel);
			}
		});
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	private boolean isNetworkWeak() {
		return networkWeak;
	}

	// FIXME 这里有问题
	@Override
	protected void physicalClose() {

		closeSSL();

		// 最后一轮 //FIXME once
		try {
			this.fireEvent(selectorEventLoop);
		} catch (IOException e) {
		}

		this.opened = false;

		this.closing = false;

		this.releaseFutures();

		this.selectionKey.attach(null);

		try {
			this.channel.close();
		} catch (Exception e) {
		}

		this.selectionKey.cancel();

		fireClosed();

		closeConnector();
	}

	private int read(ByteBuffer buffer) throws IOException {
		return channel.read(buffer);
	}

	public int read(ByteBuf buf) throws IOException {

		int length = read(buf.getNioBuffer());

		if (length > 0) {
			buf.skipBytes(length);
		}

		return length;
	}

	public void write(ByteBuf buf) throws IOException {

		int length = write(buf.getNioBuffer());

		if (length < 1) {

			downNetworkState();

			return;

		}

		buf.reverse();

		upNetworkState();
	}

	private void upNetworkState() {

		if (next_network_weak != Long.MAX_VALUE) {

			next_network_weak = Long.MAX_VALUE;

			networkWeak = false;
		}
	}

	private void downNetworkState() {

		long current = System.currentTimeMillis();

		if (next_network_weak < Long.MAX_VALUE) {

			if (networkWeak) {
				return;
			}

			if (current > next_network_weak) {

				networkWeak = true;

				fireEvent(this);
			}

		} else {

			next_network_weak = current + 64;
		}
	}

	public void fireEvent(SelectorLoopEvent event) {
		this.selectorEventLoop.dispatch(event);
	}

	private int write(ByteBuffer buffer) throws IOException {
		return channel.write(buffer);
	}

	@Override
	public boolean isPositive() {
		return !isNetworkWeak();
	}

	@Override
	protected SocketChannelThreadContext getSocketChannelThreadContext() {
		return selectorEventLoop;
	}
	
}
