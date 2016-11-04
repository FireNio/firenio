package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public class Http2FrameHeaderImpl extends AbstractIOReadFuture implements Http2FrameHeader {

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

	public Http2FrameHeaderImpl(BaseContext context) {
		super(context);
	}

	private void doHeaderComplete(Http2SocketSession session, ByteBuf buf) {

		header_complete = true;

		byte[] array = buf.array();

		int offset = buf.offset();

		this.length = ((array[offset + 0] & 0xff) << 8 * 2) 
				| ((array[offset + 1] & 0xff) << 8 * 1)
				| ((array[offset + 2] & 0xff) << 8 * 0);

		this.type = array[offset + 3] & 0xff;

		this.flags = array[offset + 4];

		this.streamIdentifier = MathUtil.byte2Int31(array, offset + 5);

		session.setLastReadFrameHeader(this);

		session.setFrameWillBeRead(type);
	}

	public boolean read(SocketSession session, ByteBuffer buffer) throws IOException {

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

	public int getLength() {
		return length;
	}

	public int getType() {
		return type;
	}

	public byte getFlags() {
		return flags;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public boolean isSilent() {
		return true;
	}

	public Http2FrameType getHttp2FrameType() {
		return Http2FrameType.FRAME_TYPE_FRAME_HEADER;
	}

	public int getStreamIdentifier() {
		return streamIdentifier;
	}

}
