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

public class Http2SettingsFrameImpl extends AbstractIOReadFuture implements Http2SettingsFrame {

	private ByteBuf	buf;

	private boolean	isComplete;

	private int[]		settings;

	public Http2SettingsFrameImpl(BaseContext context, ByteBuf buf) {
		super(context);
		this.buf = buf;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {

		isComplete = true;

		int offset = buf.offset(); 

		byte[] array = buf.array();

		int settings = buf.limit() / 6;

		for (int i = 0; i < settings; i++) {

			int _offset = i * 6;
			int key = MathUtil.byte2IntFrom2Byte(array, offset + _offset);
			int value = MathUtil.byte2Int(array, offset + _offset + 2);

			session.setSettings(key, value);
		}

		this.settings = session.getSettings();

		session.setFrameWillBeRead(Http2FrameType.FRAME_TYPE_FRAME_HEADER);
		
		session.flush(this);

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

	public int[] getSettings() {
		return settings;
	}
}
