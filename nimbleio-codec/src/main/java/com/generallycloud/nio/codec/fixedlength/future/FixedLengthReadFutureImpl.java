package com.generallycloud.nio.codec.fixedlength.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolDecoder;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.protocol.ProtocolException;

public class FixedLengthReadFutureImpl extends AbstractIOReadFuture implements FixedLengthReadFuture {

	private ByteBuf	buf;

	private String		text;

	private int		length;

	private boolean	header_complete;

	private boolean	body_complete;

	private byte[]		byteArray;

	private int		limit	= 1024 * 1024;

	public FixedLengthReadFutureImpl(IOSession session, ByteBuf buf) {
		super(session.getContext());
		this.buf = buf;
		if (isHeaderReadComplete(buf)) {
			doHeaderComplete(session, buf);
		}
	}

	public FixedLengthReadFutureImpl(NIOContext context) {
		super(context);
	}

	private boolean isHeaderReadComplete(ByteBuf buf) {
		return !buf.hasRemaining();
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

		} else if (length > limit) {

			ReleaseUtil.release(buf);

			throw new ProtocolException("max 1M ,length:" + length);

		} else if (length > buf.capacity()) {

			ReleaseUtil.release(buf);

			this.buf = session.getContext().getHeapByteBufferPool().allocate(length);

		} else {

			buf.limit(length);
		}
	}

	public boolean read(IOSession session, ByteBuffer buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (!isHeaderReadComplete(buf)) {
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

	private void doBodyComplete(ByteBuf buf) {

		body_complete = true;

		byteArray = new byte[buf.limit()];

		buf.flip();

		buf.get(byteArray);
	}

	public String getFutureName() {
		return null;
	}

	public String getText() {

		return getText(context.getEncoding());
	}

	public String getText(Charset encoding) {

		if (text == null) {
			text = new String(byteArray, encoding);
		}

		return text;
	}

	public int getLength() {
		return length;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

}
