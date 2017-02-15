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
package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class WebSocketReadFutureImpl extends AbstractChannelReadFuture implements WebSocketReadFuture {

	protected int		type;

	private boolean	eof;

	private boolean	hasMask;

	private int		length;

	private ByteBuf	buf;

	private String		serviceName;
	
	private boolean	data_complete;
	
	private boolean	header_complete;

	private boolean	remain_header_complete;

	private int		limit;

	private byte[]		mask;

	private byte[]		byteArray;

	public WebSocketReadFutureImpl(SocketSession session, ByteBuf buf,int limit) {
		super(session.getContext());

		this.limit = limit;
		
		this.buf = buf;

		this.setServiceName(session);
	}

	public WebSocketReadFutureImpl(SocketChannelContext context) {
		super(context);
	}
	
	protected WebSocketReadFutureImpl(SocketSession session) {
		super(session.getContext());
		this.type = OP_CONNECTION_CLOSE_FRAME;
		this.setServiceName(session);
	}
	
	private void setServiceName(SocketSession session){
		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
	}
	
	@Override
	public boolean isCloseFrame() {
		return OP_CONNECTION_CLOSE_FRAME == type;
	}

	private void doHeaderComplete(ByteBuf buf) {

		int remain_header_size = 0;

		byte b = buf.getByte();

		eof = ((b & 0xFF) >> 7) == 1;

		type = (b & 0xF);

		if (type == WebSocketProtocolDecoder.TYPE_PING) {
			setPING();
		} else if (type == WebSocketProtocolDecoder.TYPE_PONG) {
			setPONG();
		}

		b = buf.getByte();

		hasMask = ((b & 0xFF) >> 7) == 1;

		if (hasMask) {

			remain_header_size += 4;
		}

		length = (b & 0x7f);

		if (length < 126) {

		} else if (length == 126) {

			remain_header_size += 2;

		} else {

			remain_header_size += 4;
		}

		buf.reallocate(remain_header_size);
	}

	private void doRemainHeaderComplete(SocketSession session, ByteBuf buf) throws IOException {

		remain_header_complete = true;

		if (length < 126) {

		} else if (length == 126) {

			length = buf.getUnsignedShort();

		} else {

			length = (int) buf.getUnsignedInt();

			if (length < 0) {
				throw new IOException("too long data length");
			}
		}

		mask = buf.getBytes();

		buf.reallocate(length, limit);
	}
	
	private void doDataComplete(ByteBuf buf){
		
		byte[] array = buf.getBytes();

		if (hasMask) {

			byte[] mask = this.mask;

			for (int i = 0; i < array.length; i++) {

				array[i] = (byte) (array[i] ^ mask[i % 4]);
			}
		}

		this.byteArray = array;

		// FIXME 部分数据不是string的
		this.readText = new String(array, context.getEncoding());
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}
			
			header_complete = true;

			doHeaderComplete(buf.flip());
		}

		if (!remain_header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			remain_header_complete = true;
			
			doRemainHeaderComplete(session, buf.flip());
		}

		if (!data_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doDataComplete(buf.flip());
		}

		return true;
	}

	@Override
	public String getFutureName() {
		return serviceName;
	}

	@Override
	public boolean isEof() {
		return eof;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

	@Override
	public byte[] getByteArray() {
		return byteArray;
	}

}
