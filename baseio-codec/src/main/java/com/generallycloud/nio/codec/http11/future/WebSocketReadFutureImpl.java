package com.generallycloud.nio.codec.http11.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.http11.WebSocketProtocolDecoder;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public class WebSocketReadFutureImpl extends AbstractChannelReadFuture implements WebSocketReadFuture {

	protected int		type;

	private boolean	eof;

	private boolean	hasMask;

	private int		length;

	private ByteBuf	buf;

	private String		serviceName;
	
	private boolean	dataComplete;
	
	private boolean	headerComplete;

	private boolean	remain_header_complete;

	private int		limit;

	private byte[]		mask;

	private byte[]		byteArray;

	public WebSocketReadFutureImpl(SocketSession session, ByteBuf buf,int limit) {
		super(session.getContext());

		this.limit = limit;
		
		this.buf = buf;

		this.serviceName = (String) session.getAttribute(SESSION_KEY_SERVICE_NAME);
	}

	public WebSocketReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	private void doHeaderComplete(ByteBuf buf) {

		headerComplete = true;

		buf.flip();

		int remain_header_size = 0;

		byte b = buf.getByte();

		eof = ((b & 0xFF) >> 7) == 1;

		type = (b & 0xF);

		if (type == WebSocketProtocolDecoder.TYPE_PING) {
			setPING();
		} else if (type == WebSocketProtocolDecoder.TYPE_PONG) {
			setPONG();
		}

		b = buf.getByte();

		hasMask = ((b & 0xFF) >> 7) == 1;

		if (hasMask) {

			remain_header_size += 4;
		}

		length = (b & 0x7f);

		if (length < 126) {

		} else if (length == 126) {

			remain_header_size += 2;

		} else {

			remain_header_size += 4;
		}

		buf.limit(remain_header_size);

	}

	private void doRemainHeaderComplete(SocketSession session, ByteBuf buf) throws IOException {

		remain_header_complete = true;

		if (length < 126) {

		} else if (length == 126) {

			length = buf.getUnsignedShort();

		} else {

			length = (int) buf.getUnsignedInt();

			if (length < 0) {
				throw new IOException("too long data length");
			}
		}

		buf.flip();

		mask = buf.getBytes();

		buf.reallocate(length, limit);
	}

	public boolean read(SocketSession session, ByteBuf src) throws IOException {

		ByteBuf buf = this.buf;

		if (!headerComplete) {

			buf.read(src);

			if (buf.hasRemaining()) {
				return false;
			}

			doHeaderComplete(buf);
		}

		if (!remain_header_complete) {

			buf.read(src);

			if (buf.hasRemaining()) {
				return false;
			}

			doRemainHeaderComplete(session, buf);
		}

		if (!dataComplete) {

			buf.read(src);

			if (buf.hasRemaining()) {
				return false;
			}

			buf.flip();

			byte[] array = buf.getBytes();

			if (hasMask) {

				byte[] mask = this.mask;

				for (int i = 0; i < array.length; i++) {

					array[i] = (byte) (array[i] ^ mask[i % 4]);
				}
			}

			this.byteArray = array;

			// FIXME 部分数据不是string的
			this.readText = new String(array, context.getEncoding());

			dataComplete = true;

			return true;
		}

		return true;
	}

	public String getFutureName() {
		return serviceName;
	}

	public boolean isEof() {
		return eof;
	}

	public int getType() {
		return type;
	}

	public int getLength() {
		return length;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public byte[] getByteArray() {
		return byteArray;
	}

}
