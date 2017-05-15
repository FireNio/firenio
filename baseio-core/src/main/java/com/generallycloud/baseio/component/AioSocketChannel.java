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
import java.nio.channels.AsynchronousSocketChannel;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;

public class AioSocketChannel extends AbstractSocketChannel {

	private AsynchronousSocketChannel		channel;
	private AioSocketChannelContext		context;
	private ReadCompletionHandler			readCompletionHandler;
	private WriteCompletionHandler		writeCompletionHandler;
	private ByteBuf					readCache;
	private CachedAioThread 				aioThread;

	private static final Logger			logger			= LoggerFactory
			.getLogger(AioSocketChannel.class);

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public AioSocketChannel(CachedAioThread aioThread, AsynchronousSocketChannel channel) {
		super(aioThread);
		this.channel = channel;
		this.readCache = UnpooledByteBufAllocator.getHeapInstance().allocate(4096);
		this.context = aioThread.getChannelContext();
		this.readCompletionHandler = aioThread.getReadCompletionHandler();
		this.writeCompletionHandler = aioThread.getWriteCompletionHandler();
		this.aioThread = aioThread;
	}

	@Override
	public AioSocketChannelContext getContext() {
		return context;
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
	protected void doFlush(ChannelWriteFuture future) {
		flush(false);
	}
	
	@Override
	protected InetSocketAddress getRemoteSocketAddress0() throws IOException {
		return (InetSocketAddress) channel.getRemoteAddress();
	}

	@Override
	protected InetSocketAddress getLocalSocketAddress0() throws IOException {
		return (InetSocketAddress) channel.getLocalAddress();
	}
	
	private volatile boolean flushing;

	private void flush(boolean forceFlushing) {

		try {

			if (!forceFlushing && flushing) {
				return;
			}

			if (write_future == null) {
				write_future = write_futures.poll();
			}

			if (write_future == null) {
				flushing = false;
				return;
			}
			
			if (!isOpened()) {
				fireClosed(write_future, new ClosedChannelException("closed"));
				return;
			}

			flushing = true;

			write_future.write(this);

		} catch (IOException e) {

			fireClosed(write_future, e);
		}
	}

	private void fireClosed(ChannelWriteFuture future, IOException e) {

		if (future == null) {
			return;
		}

		future.onException(getSession(), e);
	}

	// FIXME 这里有问题
	@Override
	protected void physicalClose() {

		closeSSL();

		// 最后一轮 //FIXME once

		this.flush(false);

		this.opened = false;

		this.releaseFutures();

		try {
			channel.shutdownOutput();
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}

		try {
			channel.shutdownInput();
		} catch (IOException e) {
			logger.info(e.getMessage(), e);
		}

		CloseUtil.close(channel);
		
		fireClosed();

		closeConnector();
	}

	public void read(ByteBuf cache) {
		channel.read(cache.clear().nioBuffer(), this, readCompletionHandler);
	}

	public void write(ByteBuf buf) {
		channel.write(buf.getNioBuffer(), this, writeCompletionHandler);
	}
	
	@Override
	public void close() throws IOException {

		synchronized (getCloseLock()) {
			
			if (!isOpened()) {
				releaseFutures();
				return;
			}
			
			this.opened = false;
			
			physicalClose();
		}
		
	}
	
	// FIXME __hebing
	public void writeCallback(Integer length) {

		synchronized (getCloseLock()) {
			
			if (!isOpened()) {
				return;
			}
			
			ChannelWriteFuture write_future = this.write_future;

			write_future.getByteBuf().reverse();

			if (!write_future.isCompleted()) {
				flush(true);
				return;
			}

			writeFutureLength.getAndAdd(-write_future.getBinaryLength());

			write_future.onSuccess(session);

			write_future = null;

			flush(true);
		}

	}

	public ByteBuf getReadCache() {
		return readCache;
	}
	
	@Override
	protected SocketChannelThreadContext getSocketChannelThreadContext() {
		return aioThread;
	}
}
