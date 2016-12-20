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
package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.SocketChannel;

public class ChannelWriteFutureImpl extends FutureImpl implements ChannelWriteFuture {

	protected ReadFuture				readFuture;
	protected ByteBuf					buf;
	protected Linkable<ChannelWriteFuture>	next;

	private static final Logger			logger	= LoggerFactory.getLogger(ChannelWriteFutureImpl.class);

	public ChannelWriteFutureImpl(ReadFuture readFuture, ByteBuf buf) {
		this.readFuture = readFuture;
		this.buf = buf;
		this.buf.nioBuffer();
	}

	@Override
	public void onException(SocketSession session, Exception e) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IoEventHandle handle = readFuture.getIOEventHandle();

		try {
			handle.exceptionCaught(session, readFuture, e, IoEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1.getMessage(), e1);
		}
	}

	@Override
	public void onSuccess(SocketSession session) {

		ReadFuture readFuture = this.getReadFuture();

		ReleaseUtil.release(this);

		IoEventHandle handle = readFuture.getIOEventHandle();

		try {
			handle.futureSent(session, readFuture);
		} catch (Throwable e) {
			logger.debug(e);
		}
	}

	@Override
	public boolean write(SocketChannel channel) throws IOException {

		ByteBuf buf = this.buf;

		buf.write(channel);

		return !buf.hasRemaining();
	}

	@Override
	public ReadFuture getReadFuture() {
		return readFuture;
	}

	public void setReadFuture(ReadFuture readFuture) {
		this.readFuture = readFuture;
	}

	@Override
	public String toString() {
		return readFuture.toString();
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

	@Override
	public ChannelWriteFuture duplicate() {
		return duplicate(readFuture);
	}
	
	@Override
	public ChannelWriteFuture duplicate(ReadFuture future) {
		return new ChannelWriteFutureImpl(future, buf.duplicate());
	}

	@Override
	public Linkable<ChannelWriteFuture> getNext() {
		return next;
	}

	@Override
	public void setNext(Linkable<ChannelWriteFuture> next) {
		this.next = next;
	}

	@Override
	public ChannelWriteFuture getValue() {
		return this;
	}

	@Override
	public void wrapSSL(SocketSession session, SslHandler handler) throws IOException {

		ByteBuf old = this.buf;

		try {

			ByteBuf _buf = handler.wrap(session, old);

			if (_buf == null) {
				throw new IOException("closed ssl");
			}

			this.buf = _buf;

			this.buf.nioBuffer();

		} finally {
			ReleaseUtil.release(old);
		}
	}

	@Override
	public int getBinaryLength() {
		return buf.limit();
	}
	
}
