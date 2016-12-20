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

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractReadFutureAcceptor implements ForeReadFutureAcceptor{

	private Logger logger = LoggerFactory.getLogger(AbstractReadFutureAcceptor.class);
	
	@Override
	public void accept(final SocketSession session, final ReadFuture future) throws Exception {

		ChannelReadFuture f = (ChannelReadFuture) future;
		
		if (f.isSilent()) {
			return;
		}

		if (f.isHeartbeat()) {

			acceptHeartBeat(session, f);

			return;
		}
		
		SocketChannelContext context = session.getContext();

		IoEventHandle eventHandle = context.getIoEventHandleAdaptor();
		
		accept(eventHandle, session, f);
	}
	
	protected abstract void accept(IoEventHandle eventHandle,SocketSession session, ChannelReadFuture future);
	
	private void acceptHeartBeat(final SocketSession session, final ChannelReadFuture future) {

		if (future.isPING()) {

			logger.info("收到心跳请求!来自：{}", session);

			SocketChannelContext context = session.getContext();

			BeatFutureFactory factory = context.getBeatFutureFactory();

			if (factory == null) {

				RuntimeException e = new RuntimeException("none factory of BeatFuture");

				CloseUtil.close(session);

				logger.error(e.getMessage(), e);

				return;
			}

			ReadFuture f = factory.createPONGPacket(session);

			session.flush(f);
		} else {
			logger.info("收到心跳回报!来自：{}", session);
		}

	}

}
