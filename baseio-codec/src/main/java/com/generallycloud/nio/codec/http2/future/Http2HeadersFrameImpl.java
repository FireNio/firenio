package com.generallycloud.nio.codec.http2.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http2.Http2SocketSession;
import com.generallycloud.nio.codec.http2.hpack.Decoder;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;

public class Http2HeadersFrameImpl extends AbstractIOReadFuture implements Http2HeadersFrame {

	private ByteBuf	buf;

	private boolean	isComplete;

	private byte		padLength;

	private boolean	e;

	private int		streamDependency;

	private int		weight;

	private byte[]	fragment;
	
	private static Decoder decoder = new Decoder();

	public Http2HeadersFrameImpl(BaseContext context, ByteBuf buf) {
		super(context);
		this.buf = buf;
	}

	private void doComplete(Http2SocketSession session, ByteBuf buf) throws IOException {

		isComplete = true;
		
		buf.flip();

		Http2FrameHeader header = session.getLastReadFrameHeader();
		
		byte flags = header.getFlags();
		
		int offset = buf.offset();

		byte[] array = buf.array();
		
		int readIndex = offset;
		
		if ((flags & FLAG_PADDED)  > 0) {
			padLength = array[readIndex++];
		}
		
		if((flags & FLAG_PRIORITY) > 0){
			e = (array[readIndex] & 0x80) > 0;
			streamDependency = MathUtil.byte2Int31(array, readIndex);
			readIndex+=4;
			weight = array[readIndex++];
		}
		
		buf.skipBytes(readIndex - offset);
		
		decoder.decode(header.getStreamIdentifier(), buf, session.getHttp2Headers());
		
//		int fragmentLength = buf.position() - (readIndex - offset) - padLength;
//		
//		this.fragment = new byte[fragmentLength];
		
//		System.arraycopy(array, readIndex+1, fragment, 0, fragmentLength);

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

	public byte[] getFragment() {
		return fragment;
	}

}
