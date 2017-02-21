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
package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.AioSocketChannel;
import com.generallycloud.nio.component.AioSocketChannelContext;
import com.generallycloud.nio.component.CachedAioThread;
import com.generallycloud.nio.component.NioChannelService;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSelectorBuilder;
import com.generallycloud.nio.component.SocketSelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

/**
 * @author wangkai
 *
 */
public class SocketChannelConnector implements ChannelConnector {

	private AbstractSocketChannelConnector	_connector;
	
	private SocketChannelContext context;

	public SocketChannelConnector(SocketChannelContext context) {
		this.context = context;
		this._connector = buildConnector(context);
	}
	
	private ChannelConnector unwrap(){
		return _connector;
	}

	@Override
	public SocketSession getSession() {
		return _connector.getSession();
	}

	@Override
	public SocketSession connect() throws IOException {
		return _connector.connect();
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}
	
	@Override
	public void close() throws IOException {
		unwrap().close();
	}

	@Override
	public InetSocketAddress getServerSocketAddress() {
		return unwrap().getServerSocketAddress();
	}

	@Override
	public boolean isActive() {
		return unwrap().isActive();
	}

	@Override
	public boolean isConnected() {
		return unwrap().isConnected();
	}

	@Override
	public long getTimeout() {
		return unwrap().getTimeout();
	}

	@Override
	public void setTimeout(long timeout) {
		unwrap().setTimeout(timeout);
	}

	@Override
	public Waiter<IOException> asynchronousClose() {
		return unwrap().asynchronousClose();
	}

	abstract class AbstractSocketChannelConnector extends AbstractChannelConnector {

		protected UnsafeSocketSession		session;

		protected Waiter<Object>			waiter;

		protected void fireSessionOpend() {
			session.getSocketChannel().fireOpend();
		}

		@Override
		public SocketSession getSession() {
			return session;
		}

		@Override
		protected boolean canSafeClose() {
			return session == null
					|| (!session.inSelectorLoop() && !session.getExecutorEventLoop().inEventLoop());
		}

		@Override
		public SocketSession connect() throws IOException {

			this.waiter = new Waiter<Object>();

			this.session = null;

			this.initialize();

			return getSession();
		}
		
		protected void wait4connect() throws TimeoutException{
			
			if (waiter.await(getTimeout())) {

				CloseUtil.close(this);

				throw new TimeoutException(
						"connect to " + getServerSocketAddress().toString() + " time out");
			}

			Object o = waiter.getPayload();

			if (o instanceof Exception) {

				CloseUtil.close(this);

				Exception t = (Exception) o;

				throw new TimeoutException(MessageFormatter.format(
						"connect faild,connector:[{}],nested exception is {}", getServerSocketAddress(),
						t.getMessage()), t);
			}
			
		}
		
	}
	
	private AbstractSocketChannelConnector buildConnector(SocketChannelContext context) {
		if (context instanceof NioSocketChannelContext) {
			return new NioSocketChannelConnector((NioSocketChannelContext) context);
		} else if (context instanceof AioSocketChannelContext) {
			return new AioSocketChannelConnector((AioSocketChannelContext) context);
		}
		throw new IllegalArgumentException("context");
	}

	class AioSocketChannelConnector extends AbstractSocketChannelConnector {

		private AioSocketChannelContext context;

		public AioSocketChannelConnector(AioSocketChannelContext context) {
			this.context = context;
		}

		private Logger logger = LoggerFactory.getLogger(getClass());

		@Override
		protected void connect(InetSocketAddress socketAddress) throws IOException {

			AsynchronousChannelGroup group = context.getAsynchronousChannelGroup();

			AsynchronousSocketChannel _channel = AsynchronousSocketChannel.open(group);

			_channel.connect(socketAddress, this,
					new CompletionHandler<Void, AioSocketChannelConnector>() {

						@Override
						public void completed(Void result,
								AioSocketChannelConnector connector) {

							CachedAioThread aioThread = (CachedAioThread) Thread
									.currentThread();

							AioSocketChannel channel = new AioSocketChannel(aioThread,
									_channel);

							connector.finishConnect(channel.getSession(), null);

							aioThread.getReadCompletionHandler().completed(0, channel);
						}

						@Override
						public void failed(Throwable exc,
								AioSocketChannelConnector connector) {
							connector.finishConnect(session, exc);
						}
					});

			wait4connect();
		}

		protected void finishConnect(UnsafeSocketSession session, Throwable exception) {

			if (exception == null) {

				this.session = session;

				LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",
						getServerSocketAddress());

				fireSessionOpend();

				this.waiter.setPayload(null);

				if (waiter.isTimeouted()) {
					CloseUtil.close(this);
				}
			} else {

				this.waiter.setPayload(exception);
			}
		}

		@Override
		public AioSocketChannelContext getContext() {
			return context;
		}

		@Override
		protected void destroyService() {
		}
		
	}

	class NioSocketChannelConnector extends AbstractSocketChannelConnector implements NioChannelService {

		private NioSocketChannelContext	context;

		private SelectableChannel		selectableChannel		= null;

		private SocketSelectorBuilder		selectorBuilder		= null;

		private SelectorEventLoopGroup	selectorEventLoopGroup	= null;

		private Logger					logger				= LoggerFactory
				.getLogger(getClass());

		//FIXME 优化
		protected NioSocketChannelConnector(NioSocketChannelContext context) {
			this.selectorBuilder = new ClientNioSocketSelectorBuilder(this);
			this.context = context;
		}

		@Override
		protected void destroyService() {
			CloseUtil.close(selectableChannel);
			LifeCycleUtil.stop(selectorEventLoopGroup);
		}

		private void initSelectorLoops() {

			//FIXME socket selector event loop ?
			ServerConfiguration configuration = getContext().getServerConfiguration();

			int core_size = configuration.getSERVER_CORE_SIZE();

			int eventQueueSize = configuration.getSERVER_IO_EVENT_QUEUE();

			this.selectorEventLoopGroup = new SocketSelectorEventLoopGroup(
					(NioSocketChannelContext) getContext(), "io-process", eventQueueSize,
					core_size);
			LifeCycleUtil.start(selectorEventLoopGroup);
		}

		@Override
		protected void connect(InetSocketAddress socketAddress) throws IOException {
			
			this.initChannel();

			((SocketChannel) this.selectableChannel).connect(socketAddress);

			this.initSelectorLoops();

			wait4connect();
		}

		protected void finishConnect(UnsafeSocketSession session, Exception exception) {

			if (exception == null) {

				this.session = session;

				LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",
						getServerSocketAddress());

				fireSessionOpend();

				this.waiter.setPayload(null);

				if (waiter.isTimeouted()) {
					CloseUtil.close(this);
				}
			} else {

				this.waiter.setPayload(exception);
			}
		}

		@Override
		public NioSocketChannelContext getContext() {
			return context;
		}

		private void initChannel() throws IOException {
			this.selectableChannel = SocketChannel.open();
			this.selectableChannel.configureBlocking(false);
		}

		@Override
		public SocketSelectorBuilder getSelectorBuilder() {
			return selectorBuilder;
		}

		@Override
		public SelectableChannel getSelectableChannel() {
			return selectableChannel;
		}
	}
}
