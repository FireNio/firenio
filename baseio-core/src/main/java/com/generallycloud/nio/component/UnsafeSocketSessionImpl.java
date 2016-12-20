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

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.EmptyByteBuf;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;
import com.generallycloud.nio.protocol.EmptyReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class UnsafeSocketSessionImpl extends SocketChannelSessionImpl implements UnsafeSocketSession {

	private static final Logger	logger	= LoggerFactory.getLogger(UnsafeSocketSessionImpl.class);

	public UnsafeSocketSessionImpl(SocketChannel channel,Integer sessionID) {
		super(channel,sessionID);
	}

	@Override
	public SocketChannel getSocketChannel() {
		return channel;
	}

	@Override
	public void fireOpend() {

		if (isEnableSSL() && context.getSslContext().isClient()) {

			handshakeWaiter = new Waiter<Exception>();

			ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

			ChannelWriteFuture f = new ChannelWriteFutureImpl(future, EmptyByteBuf.EMPTY_BYTEBUF);

			flush(f);

			// wait

			if (handshakeWaiter.await(3000000)) {// FIXME test
				CloseUtil.close(this);
				throw new RuntimeException("hands shake failed");
			}

			if (handshakeWaiter.getPayload() != null) {
				throw new RuntimeException(handshakeWaiter.getPayload());
			}
			// success
		}

		Linkable<SocketSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionOpened(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}

	}

	@Override
	public void fireClosed() {

		Linkable<SocketSessionEventListener> linkable = context.getSessionEventListenerLink();

		for (; linkable != null;) {

			try {

				linkable.getValue().sessionClosed(this);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			linkable = linkable.getNext();
		}
	}

	@Override
	public void physicalClose() {

		if (isEnableSSL()) {

			sslEngine.closeOutbound();

			if (context.getSslContext().isClient()) {

				ReadFuture future = EmptyReadFuture.getEmptyReadFuture(context);

				ChannelWriteFuture f = new ChannelWriteFutureImpl(future, EmptyByteBuf.EMPTY_BYTEBUF);

				flush(f);
			}

			try {
				sslEngine.closeInbound();
			} catch (SSLException e) {
			}
		}

		fireClosed();

	}

}
