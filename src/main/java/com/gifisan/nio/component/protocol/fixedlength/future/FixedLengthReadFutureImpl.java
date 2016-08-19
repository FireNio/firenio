package com.gifisan.nio.component.protocol.fixedlength.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ProtocolException;
import com.gifisan.nio.component.protocol.future.AbstractIOReadFuture;

public class FixedLengthReadFutureImpl extends AbstractIOReadFuture implements FixedLengthReadFuture {

	private ByteBuffer	header;

	private String		text;

	private int		length;

	private boolean	header_complete;

	private boolean	body_complete;

	private ByteBuffer	body;

	private byte[]	byteArray;

	private int		limit	= 1024 * 1024;

	public FixedLengthReadFutureImpl(Session session, ByteBuffer header) {
		super(session);
		this.header = header;

		if (!header.hasRemaining()) {
			doHeaderComplete(header);
		}
	}

	public FixedLengthReadFutureImpl(Session session) {
		super(session);
	}

	private void doHeaderComplete(ByteBuffer header) {

		header_complete = true;

		length = MathUtil.byte2Int(header.array());

		if (length < 1) {
			throw new ProtocolException("illegal length:" + length);
		}

		if (length > limit) {
			throw new ProtocolException("max 1M ,length:" + length);
		}
		// FIXME limit length
		body = ByteBuffer.allocate(length);
	}

	public boolean read() throws IOException {

		if (!header_complete) {

			endPoint.read(header);

			if (header.hasRemaining()) {
				return false;
			}

			doHeaderComplete(header);
		}

		if (!body_complete) {

			endPoint.read(body);

			if (body.hasRemaining()) {
				return false;
			}

			doBodyComplete(body);
		}

		return true;
	}

	private void doBodyComplete(ByteBuffer body) {

		body_complete = true;

		byteArray = body.array();
	}

	public String getServiceName() {
		return null;
	}

	public String getText() {

		if (text == null) {
			text = new String(byteArray, session.getContext().getEncoding());
		}

		return text;
	}

	public byte[] getByteArray() {
		return byteArray;
	}
}
