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
package com.generallycloud.baseio.codec.http11.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.AbstractChannelReadFuture;
import com.generallycloud.baseio.protocol.ChannelReadFuture;

public class WebSocketReadFutureImpl extends AbstractChannelReadFuture
		implements WebSocketReadFuture {

	private int			type;

	private boolean		eof;

	private boolean		hasMask;

	private int			length;

	private ByteBuf		buf;

	private String			serviceName;

	private boolean		data_complete;

	private boolean		header_complete;

	private boolean		remain_header_complete;

	private int			limit;

	private byte[]		mask;

	private byte[]		byteArray;

	public WebSocketReadFutureImpl(SocketSession session, ByteBuf buf, int limit) {
		super(session.getContext());

		this.limit = limit;

		this.buf = buf;

		this.setServiceName(session);
	}

	public WebSocketReadFutureImpl(SocketChannelContext context) {
		super(context);
		this.type = WebSocketProtocolDecoder.TYPE_TEXT;
	}

	protected void setServiceName(SocketSession session) {
		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
	}

	@Override
	public boolean isCloseFrame() {
		return OP_CONNECTION_CLOSE_FRAME == type;
	}

	private void doHeaderComplete(ByteBuf buf) {

		int remain_header_size = 0;

		byte b = buf.getByte();

		eof  = (b & 0b10000000) > 0;

		type = (b & 0xF);

//		switch (type) {
//		case WebSocketProtocolDecoder.TYPE_PING:
//			setPING();
//			break;
//		case WebSocketProtocolDecoder.TYPE_PONG:
//			setPONG();
//			break;
//		case WebSocketProtocolDecoder.TYPE_TEXT:
//			break;
//		case WebSocketProtocolDecoder.TYPE_BINARY:
//			break;
//		case WebSocketProtocolDecoder.TYPE_CLOSE:
//			break;
//
//		default:
//			break;
//		}

		if (type == WebSocketProtocolDecoder.TYPE_PING) {
			setPING();
		} else if (type == WebSocketProtocolDecoder.TYPE_PONG) {
			setPONG();
		}

		b = buf.getByte();

		hasMask = (b & 0b10000000)> 0;

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

	private void doDataComplete(ByteBuf buf) {

		byte[] array = buf.getBytes();

		if (hasMask) {

			byte[] mask = this.mask;

			int length = array.length;

			for (int i = 0; i < length; i++) {

				array[i] = (byte) (array[i] ^ mask[i % 4]);
			}
		}

		this.byteArray = array;
		
		if (type == WebSocketProtocolDecoder.TYPE_BINARY) {
			// FIXME 处理binary
			return;
		}
		
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

	@Override
	public ChannelReadFuture setPING() {

		this.type = WebSocketProtocolDecoder.TYPE_PING;

		return super.setPING();
	}

	@Override
	public ChannelReadFuture setPONG() {

		this.type = WebSocketProtocolDecoder.TYPE_PONG;

		return super.setPONG();
	}

	protected void setType(int type) {
		this.type = type;
	}
	
	@Override
	public boolean isReleased() {
		return buf.isReleased();
	}

}
