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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.EmptyByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.IoEventHandle.IoEventState;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.concurrent.ListQueue;
import com.generallycloud.baseio.concurrent.ListQueueLink;
import com.generallycloud.baseio.connector.AbstractSocketChannelConnector;
import com.generallycloud.baseio.protocol.ChannelReadFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.baseio.protocol.EmptyReadFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;
import com.generallycloud.baseio.protocol.ReadFuture;
import com.generallycloud.baseio.protocol.SslReadFuture;

public abstract class AbstractSocketChannel extends AbstractChannel implements SocketChannel {

	protected ProtocolDecoder				protocolDecoder;
	protected ProtocolEncoder				protocolEncoder;
	protected ProtocolFactory				protocolFactory;
	protected AtomicInteger					writeFutureLength;
	protected ExecutorEventLoop				executorEventLoop;
	protected SSLEngine					sslEngine;
	protected SslHandler					sslHandler;
	protected UnsafeSocketSession			session;
	protected transient ChannelWriteFuture	write_future;
	protected transient ChannelReadFuture		readFuture;
	protected transient SslReadFuture		sslReadFuture;
	protected ListQueue<ChannelWriteFuture>	write_futures;

	private static final Logger			logger		= LoggerFactory.getLogger(AbstractSocketChannel.class);

	// FIXME 改进network wake 机制
	// FIXME network weak check
	public AbstractSocketChannel(SocketChannelThreadContext context) {
		super(context.getByteBufAllocator(), context.getChannelContext());
		SocketChannelContext socketChannelContext = context.getChannelContext();
		this.protocolFactory = socketChannelContext.getProtocolFactory();
		this.protocolDecoder = socketChannelContext.getProtocolDecoder();
		this.protocolEncoder = socketChannelContext.getProtocolEncoder();
		this.executorEventLoop = context.getExecutorEventLoop();
		this.session = context.getChannelContext().getSessionFactory().newUnsafeSession(this);
		
		// FIXME 这里最好不要用ABQ，使用链式可增可减
//		int queue_size = socketChannelContext.getServerConfiguration().getSERVER_IO_EVENT_QUEUE();
//		this.write_futures	= new ListQueueO2O<>(queue_size);
		this.write_futures = new ListQueueLink<>();
		this.writeFutureLength = new AtomicInteger();
	}

	@Override
	public int getWriteFutureLength() {
		return writeFutureLength.get();
	}

	@Override
	public InetSocketAddress getLocalSocketAddress() {
		if (local == null) {
			try {
				local = getLocalSocketAddress0();
			} catch (IOException e) {
				local = ERROR_SOCKET_ADDRESS;
			}
		}
		return local;
	}

	@Override
	public void finishHandshake(Exception e) {
		if (getContext().getSslContext().isClient()) {
			AbstractSocketChannelConnector connector =  (AbstractSocketChannelConnector) getContext().getChannelService();
			connector.finishConnect(getSession(), e);
		}
	}

	@Override
	protected String getMarkPrefix() {
		return "Tcp";
	}

	@Override
	public ProtocolDecoder getProtocolDecoder() {
		return protocolDecoder;
	}

	@Override
	public ProtocolEncoder getProtocolEncoder() {
		return protocolEncoder;
	}

	@Override
	public ProtocolFactory getProtocolFactory() {
		return protocolFactory;
	}

	@Override
	public ChannelReadFuture getReadFuture() {
		return readFuture;
	}

	@Override
	public InetSocketAddress getRemoteSocketAddress() {
		if (remote == null) {
			try {
				remote = getRemoteSocketAddress0();
			} catch (IOException e) {
				remote = ERROR_SOCKET_ADDRESS;
			}
		}
		return remote;
	}
	
	protected abstract InetSocketAddress getRemoteSocketAddress0() throws IOException;
	
	protected abstract InetSocketAddress getLocalSocketAddress0() throws IOException;

	@Override
	public UnsafeSocketSession getSession() {
		return session;
	}

	@Override
	public int getWriteFutureSize() {
		return write_futures.size();
	}

	// FIXME 是否使用channel.isOpen()
	@Override
	public boolean isOpened() {
		return opened;
	}

	@Override
	public boolean isEnableSSL() {
		return getContext().isEnableSSL();
	}

	@Override
	public SSLEngine getSSLEngine() {
		return sslEngine;
	}

	@Override
	public void flush(ChannelReadFuture future) {

		if (future == null || future.flushed()) {
			return;
		}

		if (!isOpened()) {

			future.flush();

			IoEventHandle handle = future.getIoEventHandle();

			exceptionCaught(handle, future, new ClosedChannelException(toString()),
					IoEventState.WRITE);

			return;
		}

		try {

			ProtocolEncoder encoder = getProtocolEncoder();

			ByteBufAllocator allocator = getByteBufAllocator();

			flush(encoder.encode(allocator, future.flush()));

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			IoEventHandle handle = future.getIoEventHandle();

			exceptionCaught(handle, future, e, IoEventState.WRITE);
		}
	}

	private void exceptionCaught(IoEventHandle handle, ReadFuture future, Exception cause,
			IoEventState state) {
		try {
			handle.exceptionCaught(getSession(), future, cause, state);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void flush(ChannelWriteFuture future) {

		UnsafeSocketSession session = getSession();

		synchronized (getCloseLock()) {
			
			try {

				if (!isOpened()) {
					future.onException(session, new ClosedChannelException(session.toString()));
					return;
				}
				
				if (!write_futures.offer(future)) {
					future.onException(session, new RejectedExecutionException());
					return;
				}
				
				int length = writeFutureLength.addAndGet(future.getBinaryLength());
				
				if (length > 1024 * 1024 * 10) {
					// FIXME 该连接写入过多啦
				}

				// 如果write futures > 1 说明在offer之前至少有一个write future
				// event loop 在判断complete时返回false
				if (write_futures.size() > 1) {
					return;
				}

				doFlush(future);

			} catch (Exception e) {

				future.onException(session, e);

			}
			
		}

	}

	protected abstract void doFlush(ChannelWriteFuture future);

	protected void releaseFutures() {

		ReleaseUtil.release(readFuture);
		ReleaseUtil.release(sslReadFuture);

		ClosedChannelException e = null;

		if (write_future != null && !write_future.isReleased()) {

			e = new ClosedChannelException(session.toString());

			write_future.onException(session, e);
		}

		ListQueue<ChannelWriteFuture> writeFutures = this.write_futures;

		if (writeFutures.size() == 0) {
			return;
		}

		ChannelWriteFuture f = writeFutures.poll();

		UnsafeSocketSession session = this.session;

		if (e == null) {
			e = new ClosedChannelException(session.toString());
		}

		for (; f != null;) {

			f.onException(session, e);

			ReleaseUtil.release(f);

			f = writeFutures.poll();
		}
	}

	protected void closeSSL() {

		if (isEnableSSL()) {

			sslEngine.closeOutbound();

			if (getContext().getSslContext().isClient()) {

				flush(new ChannelWriteFutureImpl(EmptyReadFuture.getInstance(),
						EmptyByteBuf.getInstance()));
			}

			try {
				sslEngine.closeInbound();
			} catch (SSLException e) {
			}
		}
	}
	
	@Override
	public SslHandler getSslHandler() {
		return sslHandler;
	}

	@Override
	public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
		this.protocolDecoder = protocolDecoder;
	}

	@Override
	public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
		this.protocolEncoder = protocolEncoder;
	}

	@Override
	public void setProtocolFactory(ProtocolFactory protocolFactory) {
		this.protocolFactory = protocolFactory;
	}

	@Override
	public void setReadFuture(ChannelReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	@Override
	public SslReadFuture getSslReadFuture() {
		return sslReadFuture;
	}

	@Override
	public void setSslReadFuture(SslReadFuture future) {
		this.sslReadFuture = future;
	}

	@Override
	public ExecutorEventLoop getExecutorEventLoop() {
		return executorEventLoop;
	}
	
	protected abstract SocketChannelThreadContext getSocketChannelThreadContext();

	@Override
	public void fireOpend() {

		SocketChannelContext context = getContext();

		if (context.isEnableSSL()) {
			this.sslHandler = getSocketChannelThreadContext().getSslHandler();
			this.sslEngine = context.getSslContext().newEngine();
		}

		if (isEnableSSL() && context.getSslContext().isClient()) {

			flush(new ChannelWriteFutureImpl(EmptyReadFuture.getInstance(),
					EmptyByteBuf.getInstance()));
		}

		Linkable<SocketSessionEventListener> linkable = context.getSessionEventListenerLink();

		UnsafeSocketSession session = getSession();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionOpened(session);

			} catch (Exception e) {
				logger.errorDebug(e);
				CloseUtil.close(this);
				break;
			}

			linkable = linkable.getNext();
		}
	}

	protected void fireClosed() {

		Linkable<SocketSessionEventListener> linkable = getContext()
				.getSessionEventListenerLink();

		UnsafeSocketSession session = getSession();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionClosed(session);

			} catch (Exception e) {
				logger.errorDebug(e);
			}
			linkable = linkable.getNext();
		}
	}
	
	@Override
	public boolean inSelectorLoop() {
		return getSocketChannelThreadContext().inEventLoop();
	}

}
