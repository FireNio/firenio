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
package com.generallycloud.baseio.protocol;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.Linkable;
import com.generallycloud.baseio.component.SocketChannel;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.IoEventHandle.IoEventState;
import com.generallycloud.baseio.component.ssl.SslHandler;

public class ChannelWriteFutureImpl extends AbstractFuture implements ChannelWriteFuture {

	protected ReadFuture				readFuture;
	protected ByteBuf					buf;
	protected Linkable<ChannelWriteFuture>	next;
	protected boolean					needSSL;

	private static final Logger			logger	= LoggerFactory.getLogger(ChannelWriteFutureImpl.class);

	public ChannelWriteFutureImpl(ReadFuture readFuture, ByteBuf buf) {
		this.readFuture = readFuture;
		this.buf = buf;
		this.buf.nioBuffer();
		this.needSSL = readFuture.getContext().isEnableSSL();
	}
	
	@Override
	public void onException(SocketSession session, Exception e) {
		
		ReleaseUtil.release(this);

		ReadFuture readFuture = this.getReadFuture();

		IoEventHandle handle = readFuture.getIoEventHandle();

		try {
			handle.exceptionCaught(session, readFuture, e, IoEventState.WRITE);
		} catch (Throwable e1) {
			logger.debug(e1.getMessage(), e1);
		}
	}

	@Override
	public void onSuccess(SocketSession session) {
		
		ReleaseUtil.release(this);

		ReadFuture readFuture = this.getReadFuture();

		IoEventHandle handle = readFuture.getIoEventHandle();

		try {
			handle.futureSent(session, readFuture);
		} catch (Throwable e) {
			logger.debug(e);
		}
	}

	@Override
	public void write(SocketChannel channel) throws IOException {
		if (needSSL) {
			needSSL = false;
			wrapSSL(channel);
		}
		channel.write(buf);
	}
	
	@Override
	public boolean isCompleted() {
		return !buf.hasRemaining();
	}

	@Override
	public ReadFuture getReadFuture() {
		return readFuture;
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

	private void wrapSSL(SocketChannel channel) throws IOException {
		// FIXME 部分情况下可以不在业务线程做wrapssl
		ByteBuf old = this.buf;
		
		SslHandler handler = channel.getSslHandler();

		try {

			ByteBuf _buf = handler.wrap(channel, old);

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
	
	@Override
	public ByteBuf getByteBuf() {
		return buf;
	}
	
	@Override
	public boolean isReleased() {
		return buf.isReleased();
	}
	
}
