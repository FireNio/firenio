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
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.AioSocketChannel;
import com.generallycloud.nio.component.AioSocketChannelContext;
import com.generallycloud.nio.component.CachedAioThread;
import com.generallycloud.nio.protocol.ReadFuture;

/**
 * @author wangkai
 *
 */
public class AioChannelAcceptor extends AbstractChannelAcceptor implements ChannelAcceptor{
	
	private AioSocketChannelContext context ;
	
	private AsynchronousServerSocketChannel serverSocketChannel;
	
	public AioChannelAcceptor(AioSocketChannelContext context) {
		this.context = context;
	}

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void broadcast(ReadFuture future) {
		//FIXME _____aio broadcast
	}

	@Override
	protected void bind(InetSocketAddress socketAddress) throws IOException {
		
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
	protected void destroyChannel() {
		CloseUtil.close(serverSocketChannel);
	}
	
	@Override
	public AioSocketChannelContext getContext() {
		return context;
	}
	
	
}
