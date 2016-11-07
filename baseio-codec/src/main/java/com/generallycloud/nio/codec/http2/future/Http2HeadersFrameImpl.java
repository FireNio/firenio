package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.codec.http2.hpack.Decoder;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketSession;

public class Http2HeadersFrameImpl extends AbstractHttp2Frame implements Http2HeadersFrame {

	private ByteBuf	buf;

	private boolean	isComplete;

	private byte		padLength;

	private boolean	e;

	private int		streamDependency;

	private int		weight;

	private boolean endStream;
	
	private static Decoder decoder = new Decoder();

	public Http2HeadersFrameImpl(Http2SocketSession session, ByteBuf buf) {
		super(session);
		this.buf = buf;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {

		this.isComplete = true;
		
		buf.flip();

		byte flags = this.flags;
		
		this.endStream = (flags & FLAG_END_STREAM) > 0;
		
		if ((flags & FLAG_PADDED)  > 0) {
			padLength = buf.getByte();
		}
		
		if((flags & FLAG_PRIORITY) > 0){
			
			streamDependency = buf.getInt();
			
			e = streamDependency < 0;
			
			if (e) {
				streamDependency = streamDependency & 0x7FFFFFFF;
			}
			
			weight = buf.getByte();
		}
		
		decoder.decode(streamDependency, buf, session.getHttp2Headers());
		
		session.setFrameWillBeRead(Http2FrameType.FRAME_TYPE_FRAME_HEADER);
	}

	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

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
		return !endStream;
	}

	public Http2FrameType getHttp2FrameType() {
		return Http2FrameType.FRAME_TYPE_HEADERS;
	}

	public boolean isE() {
		return e;
	}

	public int getStreamDependency() {
		return streamDependency;
	}

	public int getWeight() {
		return weight;
	}

	public byte getPadLength() {
		return padLength;
	}
	
}
