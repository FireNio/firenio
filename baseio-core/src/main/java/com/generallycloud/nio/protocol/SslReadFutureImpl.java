package com.generallycloud.nio.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.common.ssl.SslUtils;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Session;

public class SslReadFutureImpl extends AbstractIOReadFuture implements SslReadFuture {

	private ByteBuf	buf;

	private int		length;

	private boolean	header_complete;

	private boolean	body_complete;

	private int		limit;

	public SslReadFutureImpl(IOSession session, ByteBuf buf) {
		this(session, buf, 1024 * 1024);
	}

	public SslReadFutureImpl(IOSession session, ByteBuf buf, int limit) {
		super(session.getContext());
		this.buf = buf;
		this.limit = limit;
	}

	private boolean isHeaderReadComplete(ByteBuf buf) {
		return !buf.hasRemaining();
	}

	private void doHeaderComplete(Session session, ByteBuf buf) throws IOException {

		header_complete = true;

		int length = SslUtils.getEncryptedPacketLength(buf.array(), buf.offset());
		
		System.out.println("******************"+length);

		if (length < 1) {

			throw new ProtocolException("illegal length:" + length);

		} else if (length <= limit) {

			if (length > buf.capacity()) {

				this.buf = allocate(length);

				buf.flip();

				this.buf.read(buf.getMemory());

				ReleaseUtil.release(buf);

			} else {

				int pos = buf.position();

				buf.limit(length).position(pos);
			}

		} else {

			throw new ProtocolException("max " + limit + " ,length:" + length);
		}

		this.length = length;
	}

	public boolean read(IOSession session, ByteBuffer buffer) throws IOException {

		if (!header_complete) {
			
			ByteBuf buf = this.buf;

			buf.read(buffer);

			if (!isHeaderReadComplete(buf)) {
				return false;
			}

			doHeaderComplete(session, buf);
		}

		if (!body_complete) {
			
			ByteBuf buf = this.buf;

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doBodyComplete(session, buf);
		}

		return true;
	}

	private void doBodyComplete(IOSession session, ByteBuf buf) throws IOException {

		body_complete = true;

		buf.flip();
		
		SslHandler handler = context.getSslContext().getSslHandler();
		
		ByteBuf old = this.buf;
		
		try {
		
			this.buf = handler.unwrap(session, buf);
			
		} finally {
			ReleaseUtil.release(old);
		}
	}

	public ByteBuffer getMemory() {
		
		if (buf == null) {
			return null;
		}
		
		return buf.getMemory();
	}

	public int getLength() {
		return length;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

}
