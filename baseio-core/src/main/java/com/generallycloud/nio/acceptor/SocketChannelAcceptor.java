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
package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;
import java.util.Map;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
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
import com.generallycloud.nio.component.SocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

/**
 * @author wangkai
 *
 */
public class SocketChannelAcceptor implements ChannelAcceptor{

	private AbstractSocketChannelAcceptor _channelAcceptor;
	
	public SocketChannelAcceptor(SocketChannelContext context) {
		this._channelAcceptor = buildConnector(context);
	}

	private AbstractSocketChannelAcceptor unwrap(){
		return _channelAcceptor;
	}
	
	@Override
	public void unbind() throws IOException {
		unwrap().unbind();
	}

	@Override
	public Waiter<IOException> asynchronousUnbind() throws IOException {
		return unwrap().asynchronousUnbind();
	}

	@Override
	public SocketChannelContext getContext() {
		return unwrap().getContext();
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
	public void bind() throws IOException {
		unwrap().bind();
	}

	@Override
	public void broadcast(ReadFuture future) {
		unwrap().broadcast(future);
	}

	@Override
	public int getManagedSessionSize() {
		return unwrap().getManagedSessionSize();
	}
	
	
	private AbstractSocketChannelAcceptor buildConnector(SocketChannelContext context) {
		if (context instanceof NioSocketChannelContext) {
			return new NioSocketChannelAcceptor((NioSocketChannelContext) context);
		} else if (context instanceof AioSocketChannelContext) {
			return new AioSocketChannelAcceptor((AioSocketChannelContext) context);
		}
		throw new IllegalArgumentException("context");
	}
	
	abstract class AbstractSocketChannelAcceptor extends AbstractChannelAcceptor{
		
		private SocketChannelContext context;
		
		AbstractSocketChannelAcceptor(SocketChannelContext context) {
			this.context = context;
		}

		@Override
		public SocketChannelContext getContext() {
			return context;
		}
	}
	
	class AioSocketChannelAcceptor extends AbstractSocketChannelAcceptor {
		
		private AsynchronousServerSocketChannel serverSocketChannel;
		
		public AioSocketChannelAcceptor(AioSocketChannelContext context) {
			super(context);
		}

		private Logger logger = LoggerFactory.getLogger(getClass());

		@Override
		public void broadcast(ReadFuture future) {
			//FIXME _____aio broadcast
		}

		@Override
		protected void bind(InetSocketAddress socketAddress) throws IOException {
			
			AioSocketChannelContext context = (AioSocketChannelContext) getContext();
			
			AsynchronousChannelGroup group = context.getAsynchronousChannelGroup();
			
			serverSocketChannel = AsynchronousServerSocketChannel.open(group);
			
			serverSocketChannel.bind(socketAddress);

			serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {

				@Override
				public void completed(AsynchronousSocketChannel _channel, Void attachment) {

					serverSocketChannel.accept(null, this); // 接受下一个连接

					CachedAioThread aioThread = (CachedAioThread) Thread.currentThread();
					
					AioSocketChannel channel = new AioSocketChannel(aioThread, _channel);

					channel.fireOpend();

					aioThread.getReadCompletionHandler().completed(0, channel);
				}

				@Override
				public void failed(Throwable exc, Void attachment) {
					logger.error(exc.getMessage(),exc);
				}
			});
			
			logger.info("22222222222222222");
		}
		
		@Override
		protected void destroyService() {
			CloseUtil.close(serverSocketChannel);
		}
	}
	
	class NioSocketChannelAcceptor extends AbstractSocketChannelAcceptor implements NioChannelService{

		private ServerSocket			serverSocket			= null;

		private SelectableChannel		selectableChannel		= null;

		private SocketSelectorBuilder		selectorBuilder		= null;

		private SelectorEventLoopGroup	selectorEventLoopGroup	= null;

		private Logger					logger				= LoggerFactory
				.getLogger(getClass());

		public NioSocketChannelAcceptor(SocketChannelContext context) {
			super(context);
			this.selectorBuilder = new ServerNioSocketSelectorBuilder();
		}

		@Override
		protected void bind(InetSocketAddress socketAddress) throws IOException {
			
			initChannel();

			try {
				// 进行服务的绑定
				this.serverSocket.bind(socketAddress, 50);
			} catch (BindException e) {
				throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
			}

			initSelectorLoops();
		}

		@Override
		public void broadcast(final ReadFuture future) {

			offerSessionMEvent(new SocketSessionManagerEvent() {

				@Override
				public void fire(SocketChannelContext context,
						Map<Integer, SocketSession> sessions) {

					ProtocolEncoder encoder = context.getProtocolEncoder();

					ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeapInstance();

					ChannelWriteFuture writeFuture;
					try {
						writeFuture = encoder.encode(allocator, (ChannelReadFuture) future);
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
						return;
					}

					Collection<SocketSession> ss = sessions.values();

					for (SocketSession s : ss) {

						s.flush(writeFuture.duplicate());
					}

					ReleaseUtil.release(writeFuture);
				}
			});
		}

		private void initChannel() throws IOException {
			// 打开服务器套接字通道
			this.selectableChannel = ServerSocketChannel.open();
			// 服务器配置为非阻塞
			this.selectableChannel.configureBlocking(false);
			// 检索与此通道关联的服务器套接字
			this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();
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

		public void offerSessionMEvent(SocketSessionManagerEvent event) {
			getContext().getSessionManager().offerSessionMEvent(event);
		}
		
		@Override
		protected void destroyService() {
			CloseUtil.close(serverSocket);
			CloseUtil.close(selectableChannel);
			LifeCycleUtil.stop(selectorEventLoopGroup);
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
