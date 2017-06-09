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
package com.generallycloud.baseio.acceptor;

import java.util.Collection;
import java.util.Map;

import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.AbstractSocketSessionManager.SocketSessionManagerEvent;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionManager;
import com.generallycloud.baseio.protocol.ChannelReadFuture;
import com.generallycloud.baseio.protocol.ChannelWriteFuture;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ReadFuture;

/**
 * @author wangkai
 */
public abstract class AbstractSocketChannelAcceptor extends AbstractChannelAcceptor {

	private Logger				logger	= LoggerFactory.getLogger(getClass());

	private SocketChannelContext	context;
	
	private SocketSessionManager socketSessionManager;
	

	AbstractSocketChannelAcceptor(SocketChannelContext context) {
		this.context = context;
		this.socketSessionManager = context.getSessionManager();
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	public void broadcast(ReadFuture future) {

		ProtocolEncoder encoder = context.getProtocolEncoder();

		ByteBufAllocator allocator = UnpooledByteBufAllocator.getHeapInstance();

		ChannelWriteFuture writeFuture;
		try {
			writeFuture = encoder.encode(allocator, (ChannelReadFuture) future);
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return;
		}

		broadcast(writeFuture);
	}
	
	public void broadcast(final ChannelWriteFuture future) {
		
		socketSessionManager.offerSessionMEvent(new SocketSessionManagerEvent() {
			
			@Override
			public void fire(SocketChannelContext context, Map<Integer, SocketSession> sessions) {
				
				if (sessions.isEmpty()) {
					ReleaseUtil.release(future);
					return;
				}
				
				Collection<SocketSession> ss = sessions.values();

				for (SocketSession s : ss) {

					s.flush(future.duplicate());
				}

				ReleaseUtil.release(future);
			}
		});
	}

	@Override
	public int getManagedSessionSize() {
		return socketSessionManager.getManagedSessionSize();
	}
}
