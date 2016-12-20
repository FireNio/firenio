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
package com.generallycloud.nio.codec.fixedlength.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolDecoder;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;
import com.generallycloud.nio.protocol.ProtocolException;

//FIXME 2 byte header
public class FixedLengthReadFutureImpl extends AbstractChannelReadFuture implements FixedLengthReadFuture {

	private ByteBuf	buf;

	private int		length;

	private boolean	header_complete;

	private boolean	body_complete;

	private byte[]	byteArray;

	private int		limit;

	public FixedLengthReadFutureImpl(SocketSession session, ByteBuf buf,int limit) {
		super(session.getContext());
		this.buf = buf;
		this.limit = limit;
	}

	public FixedLengthReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	private void doHeaderComplete(Session session, ByteBuf buf) {

		header_complete = true;

		int length = buf.getInt(0);

		this.length = length;

		if (length < 1) {

			if (length == FixedLengthProtocolDecoder.PROTOCOL_PING) {

				setPING();

				body_complete = true;

				return;
			} else if (length == FixedLengthProtocolDecoder.PROTOCOL_PONG) {

				setPONG();

				body_complete = true;

				return;
			}

			throw new ProtocolException("illegal length:" + length);
		} 
		
		buf.reallocate(length, limit);
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doHeaderComplete(session, buf);
		}

		if (!body_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doBodyComplete(buf);
		}

		return true;
	}

	//FIXME decode
	private void doBodyComplete(ByteBuf buf) {

		body_complete = true;

		byteArray = new byte[buf.limit()];

		buf.flip();

		buf.get(byteArray);
		
		readText = new String(byteArray, context.getEncoding());
	}

	public String getFutureName() {
		return null;
	}

	public int getLength() {
		return length;
	}

	@Override
	public byte[] getByteArray() {
		return byteArray;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

}
