package com.generallycloud.nio.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketSession;

public class SslReadFutureImpl extends AbstractIOReadFuture implements SslReadFuture {

	public static final int	SSL_CONTENT_TYPE_ALERT					= 21;

	public static final int	SSL_CONTENT_TYPE_APPLICATION_DATA		= 23;

	public static final int	SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC		= 20;

	public static final int	SSL_CONTENT_TYPE_HANDSHAKE				= 22;

	public static final int	SSL_RECORD_HEADER_LENGTH				= 5;

	private boolean		body_complete;

	private ByteBuf		buf;

	private boolean		header_complete;

	private int			length;

	private int			limit;

	public SslReadFutureImpl(SocketSession session, ByteBuf buf) {
		this(session, buf, 1024 * 1024);
	}

	public SslReadFutureImpl(SocketSession session, ByteBuf buf, int limit) {
		super(session.getContext());
		this.buf = buf;
		this.limit = limit;
	}

	private void doBodyComplete(SocketSession session, ByteBuf buf) throws IOException {

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

	private void doHeaderComplete(Session session, ByteBuf buf) throws IOException {

		header_complete = true;

		int length = getEncryptedPacketLength(buf.array(), buf.offset());

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

	private int getEncryptedPacketLength(byte[] data, int offset) {
		int packetLength = 0;

		// SSLv3 or TLS - Check ContentType

		int h1 = data[offset] & 0xff;

		boolean tls;
		switch (h1) {
		case SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC:
		case SSL_CONTENT_TYPE_ALERT:
		case SSL_CONTENT_TYPE_HANDSHAKE:
		case SSL_CONTENT_TYPE_APPLICATION_DATA:
			tls = true;
			break;
		default:
			// SSLv2 or bad data
			tls = false;
		}

		if (tls) {
			// SSLv3 or TLS - Check ProtocolVersion
			int majorVersion = data[offset + 1] & 0xff;
			if (majorVersion == 3) {
				// SSLv3 or TLS
				packetLength = MathUtil.byte2IntFrom2Byte(data, offset + 3) + SSL_RECORD_HEADER_LENGTH;
				if (packetLength <= SSL_RECORD_HEADER_LENGTH) {
					// Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
					tls = false;
				}
			} else {
				// Neither SSLv3 or TLSv1 (i.e. SSLv2 or bad data)
				tls = false;
			}
		}

		if (!tls) {
			// SSLv2 or bad data - Check the version
			int headerLength = (data[offset + 4] & 0x80) != 0 ? 2 : 3;
			int majorVersion = data[offset + headerLength + 5 + 1];
			if (majorVersion == 2 || majorVersion == 3) {
				// SSLv2
				if (headerLength == 2) {
					packetLength = (MathUtil.byte2IntFrom2Byte(data, offset + 6) & 0x7FFF) + 2;
				} else {
					packetLength = (MathUtil.byte2IntFrom2Byte(data, offset + 6) & 0x3FFF) + 3;
				}
				if (packetLength <= headerLength) {
					return -1;
				}
			} else {
				return -1;
			}
		}
		return packetLength;
	}

	public int getLength() {
		return length;
	}

	public ByteBuffer getMemory() {
		
		if (buf == null) {
			return null;
		}
		
		return buf.getMemory();
	}

	private boolean isHeaderReadComplete(ByteBuf buf) {
		return !buf.hasRemaining();
	}

	public boolean read(SocketSession session, ByteBuffer buffer) throws IOException {

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

	public void release() {
		ReleaseUtil.release(buf);
	}

}
