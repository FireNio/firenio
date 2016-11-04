package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketSession;

public class Http2WindowUpdateFrameImpl extends AbstractHttp2Frame implements Http2WindowUpdateFrame {

	private ByteBuf	buf;

	private boolean	isComplete;

	private int		updateValue;

	public Http2WindowUpdateFrameImpl(Http2SocketSession session, ByteBuf buf) {
		super(session);
		this.buf = buf;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {

		isComplete = true;

		int offset = buf.offset();

		byte[] array = buf.array();

		this.updateValue = MathUtil.byte2Int31(array, offset);

		session.setFrameWillBeRead(Http2FrameType.FRAME_TYPE_FRAME_HEADER);

	}

	public boolean read(SocketSession session, ByteBuffer buffer) throws IOException {

		if (!isComplete) {

			ByteBuf buf = this.buf;

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doComplete((Http2SocketSession) session, buf);
		}

		return true;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public boolean isSilent() {
		return true;
	}

	public Http2FrameType getHttp2FrameType() {
		return Http2FrameType.FRAME_TYPE_SETTINGS;
	}

	public int getUpdateValue() {
		return updateValue;
	}

}
