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
package com.generallycloud.baseio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.codec.http2.Http2SocketSession;
import com.generallycloud.baseio.common.MathUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.AbstractChannelReadFuture;

public class Http2FrameHeaderImpl extends AbstractChannelReadFuture implements Http2FrameHeader {

	private ByteBuf		buf;

	private boolean		header_complete;

	private byte			flags;

	private int			streamIdentifier;

	private SocketHttp2Frame	frame;

	public Http2FrameHeaderImpl(SocketSession session, ByteBuf buf) {
		super(session.getContext());
		this.buf = buf;
	}

	public Http2FrameHeaderImpl(SocketChannelContext context) {
		super(context);
	}

	private void doHeaderComplete(Http2SocketSession session, ByteBuf buf) {

		byte b0 = buf.getByte();
		byte b1 = buf.getByte();
		byte b2 = buf.getByte();

		int length = ((b0 & 0xff) << 8 * 2)
					| ((b1 & 0xff) << 8 * 1) 
					| ((b2 & 0xff) << 8 * 0);

		int type = buf.getUnsignedByte();

		this.flags = buf.getByte();

		this.streamIdentifier = MathUtil.int2int31(buf.getInt());

		this.frame = genFrame(session, type, length);
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

			doHeaderComplete((Http2SocketSession) session, buf.flip());
		}

		return frame.read(session, buffer);
	}

	@Override
	public byte getFlags() {
		return flags;
	}

	@Override
	public void release() {
		ReleaseUtil.release(buf);
		ReleaseUtil.release(frame);
	}

	@Override
	public boolean isSilent() {
		return frame.isSilent();
	}

	@Override
	public Http2FrameType getHttp2FrameType() {
		return frame.getHttp2FrameType();
	}

	@Override
	public int getStreamIdentifier() {
		return streamIdentifier;
	}

	@Override
	public Http2Frame getFrame() {
		return frame;
	}

	private SocketHttp2Frame genFrame(Http2SocketSession session, Http2FrameType type, int length) {

		switch (type) {
		case FRAME_TYPE_CONTINUATION:

			break;
		case FRAME_TYPE_DATA:

			break;
		case FRAME_TYPE_GOAWAY:

			break;
		case FRAME_TYPE_HEADERS:
			return new Http2HeadersFrameImpl(session, allocate(session, length), this);
		case FRAME_TYPE_PING:

			break;
		case FRAME_TYPE_PRIORITY:

			break;
		case FRAME_TYPE_PUSH_PROMISE:

			break;
		case FRAME_TYPE_RST_STREAM:

			break;
		case FRAME_TYPE_SETTINGS:
			return new Http2SettingsFrameImpl(session, allocate(session, length), this);
		case FRAME_TYPE_WINDOW_UPDATE:
			return new Http2WindowUpdateFrameImpl(session, allocate(session, length), this);
		default:

			break;
		}
		throw new IllegalArgumentException(type.toString());
	}

	private SocketHttp2Frame genFrame(Http2SocketSession session, int type, int length) {
		return genFrame(session, Http2FrameType.getValue(type), length);
	}

}
