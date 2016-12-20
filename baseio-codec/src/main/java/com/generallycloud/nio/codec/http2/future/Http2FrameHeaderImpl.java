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
package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class Http2FrameHeaderImpl extends AbstractChannelReadFuture implements Http2FrameHeader {

	private ByteBuf	buf;

	private int		length;

	private boolean	header_complete;

	private int		type;

	private byte		flags;

	private int		streamIdentifier;

	public Http2FrameHeaderImpl(SocketSession session, ByteBuf buf) {
		super(session.getContext());
		this.buf = buf;
	}

	public Http2FrameHeaderImpl(SocketChannelContext context) {
		super(context);
	}

	private void doHeaderComplete(Http2SocketSession session, ByteBuf buf) {

		header_complete = true;
		
		buf.flip();
		
		byte b0 = buf.getByte();
		byte b1 = buf.getByte();
		byte b2 = buf.getByte();

		this.length = ((b0 & 0xff) << 8 * 2) 
				| ((b1 & 0xff) << 8 * 1)
				| ((b2 & 0xff) << 8 * 0);

		this.type = buf.getUnsignedByte();

		this.flags = buf.getByte();

		this.streamIdentifier = MathUtil.int2int31(buf.getInt());

		session.setLastReadFrameHeader(this);

		session.setFrameWillBeRead(type);
	}

	@Override
	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doHeaderComplete((Http2SocketSession) session, buf);
		}

		return true;
	}

	@Override
	public int getLength() {
		return length;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public byte getFlags() {
		return flags;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
	}

	@Override
	public boolean isSilent() {
		return true;
	}

	public Http2FrameType getHttp2FrameType() {
		return Http2FrameType.FRAME_TYPE_FRAME_HEADER;
	}

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}

}
