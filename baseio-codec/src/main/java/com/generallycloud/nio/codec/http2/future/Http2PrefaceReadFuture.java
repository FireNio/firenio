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
package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ChannelWriteFutureImpl;

public class Http2PrefaceReadFuture extends AbstractChannelReadFuture {

	private ByteBuf	buf;

	private boolean	isComplete;
	
	private static byte [] PREFACE_BINARY = "PRI * HTTP/2.0\r\n\r\nSM\r\n\r\n".getBytes(); 
	
	private static ByteBuf PREFACE_BUF;
	
	static{
		
		PREFACE_BUF = UnpooledByteBufAllocator.getHeapInstance().wrap(ByteBuffer.wrap(PREFACE_BINARY));
	}

	public Http2PrefaceReadFuture(SocketChannelContext context,ByteBuf buf) {
		super(context);
		this.buf = buf;
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {
		
		session.setPrefaceRead(false);

		if (!isPreface(buf)) {
			throw new IOException("not http2 preface");
		}
		
		ChannelWriteFuture f = new ChannelWriteFutureImpl(this, PREFACE_BUF.duplicate());
		
		session.flush(f);
	}
	
	private boolean isPreface(ByteBuf buf){
		
		if(PREFACE_BINARY.length > buf.remaining()){
			return false;
		}
		
		for (int i = 0; i < PREFACE_BINARY.length; i++) {
			
			if(PREFACE_BINARY[i] != buf.getByte()){
				return false;
			}
		}
		
		return true;
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!isComplete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}
			
			this.isComplete = true;

			doComplete((Http2SocketSession) session, buf.flip());
		}

		return true;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

}
