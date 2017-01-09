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
package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.util.Collection;
import java.util.Map;

import com.generallycloud.nio.buffer.ByteBufAllocator;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.SocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public final class SocketChannelAcceptor extends AbstractChannelAcceptor {

	private SocketChannelContext	context		= null;

	private Logger				logger		= LoggerFactory.getLogger(SocketChannelAcceptor.class);

	private ServerSocket		serverSocket	= null;

	public SocketChannelAcceptor(SocketChannelContext context) {
		this.context = context;
	}

	@Override
	protected void bind(InetSocketAddress socketAddress) throws IOException {

		try {
			// 进行服务的绑定
			this.serverSocket.bind(socketAddress, 50);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops(new ServerSocketChannelSELFactory(this));
	}

	@Override
	public void broadcast(final ReadFuture future) {

		offerSessionMEvent(new SocketSessionManagerEvent() {

			@Override
			public void fire(SocketChannelContext context, Map<Integer, SocketSession> sessions) {

				ProtocolEncoder encoder = context.getProtocolEncoder();

				ByteBufAllocator allocator = UnpooledByteBufAllocator.getInstance();

				ChannelWriteFuture writeFuture;
				try {
					writeFuture = encoder.encode(allocator, (ChannelReadFuture) future);
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					return;
				}
				
				Collection<SocketSession> ss = sessions.values();
				
				for(SocketSession s : ss){
					
					s.flush(writeFuture.duplicate());
				}

				ReleaseUtil.release(writeFuture);
			}
		});
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	protected void initselectableChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();

	}

	public void offerSessionMEvent(SocketSessionManagerEvent event) {
		getContext().getSessionManager().offerSessionMEvent(event);
	}

	@Override
	public int getManagedSessionSize() {
		return getContext().getSessionManager().getManagedSessionSize();
	}

}
