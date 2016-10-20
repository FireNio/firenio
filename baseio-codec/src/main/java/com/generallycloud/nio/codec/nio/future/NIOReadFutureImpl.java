package com.generallycloud.nio.codec.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.nio.NIOProtocolDecoder;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.DefaultParameters;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.protocol.IOWriteFuture;

/**
 *
 */
public class NIOReadFutureImpl extends AbstractIOReadFuture implements NIOReadFuture {

	private byte[]			binary;
	private int				binaryLength;
	private boolean			body_complete;
	private ByteBuf			buf;
	private Integer			futureID;
	private String				futureName;
	private int				hashCode;
	private boolean			header_complete;
	private Parameters			parameters;
	private int				service_name_length;
	private String				text;
	private int				textLength;
	private boolean			translated;
	private BufferedOutputStream	writeBinaryBuffer;

	// for ping & pong
	public NIOReadFutureImpl(NIOContext context) {
		super(context);
	}

	public NIOReadFutureImpl(NIOContext context,Integer futureID, String futureName) {
		super(context);
		this.futureName = futureName;
		this.futureID = futureID;
	}

	public NIOReadFutureImpl(IOSession session, ByteBuf buf) throws IOException {
		super(session.getContext());
		this.buf = buf;
		if (!buf.hasRemaining()) {
			doHeaderComplete(session,buf);
		}
	}

	public NIOReadFutureImpl(NIOContext context,String futureName) {
		super(context);
		this.futureName = futureName;
	}

	private void doBodyComplete(Session session,ByteBuf buf) {

		body_complete = true;

		buf.flip();

		Charset charset = session.getEncoding();

		int offset = buf.offset();

		ByteBuffer memory = buf.getMemory();

		int src_pos = memory.position();

		int src_limit = memory.limit();

		memory.limit(offset + service_name_length);

		futureName = StringUtil.decode(charset, memory);

		memory.limit(memory.position() + textLength);

		text = StringUtil.decode(charset, memory);

		memory.position(src_pos);

		memory.limit(src_limit);

		this.gainBinary(buf, offset);
	}

	private void doHeaderComplete(Session session,ByteBuf buf) throws IOException {

		header_complete = true;

		byte[] header_array = buf.array();

		int offset = buf.offset();

		int service_name_length = (header_array[offset] & 0x3f);

		int textLength = gainTextLength(header_array, offset);

		int binaryLength = gainBinaryLength(header_array, offset);

		int all_length = service_name_length + textLength + binaryLength;

		this.service_name_length = service_name_length;

		this.textLength = textLength;

		this.binaryLength = binaryLength;

		this.futureID = gainFutureID(header_array, offset);
		
		this.hashCode = gainHashCode(header_array, offset);

		if (buf.capacity() >= all_length) {

			buf.limit(all_length);

		} else {

			ReleaseUtil.release(buf);

			if (all_length > 1024 * 1024 * 2) {
				throw new IOException("max length 1024 * 1024 * 2,length=" + all_length);
			}

			this.buf = session.getContext().getHeapByteBufferPool().allocate(all_length);
		}
	}

	private void gainBinary(ByteBuf buffer, int offset) {

		if (binaryLength < 1) {
			return;
		}

		byte[] array = buffer.array();

		this.binary = new byte[binaryLength];

		System.arraycopy(array, offset + buffer.limit() - binaryLength, binary, 0, binaryLength);
	}

	private int gainBinaryLength(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + NIOProtocolDecoder.BINARY_BEGIN_INDEX);
	}

	private int gainFutureID(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + NIOProtocolDecoder.FUTURE_ID_BEGIN_INDEX);
	}
	
	private int gainHashCode(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + NIOProtocolDecoder.HASH_BEGIN_INDEX);
	}

	private int gainTextLength(byte[] header, int offset) {
		return MathUtil.byte2IntFrom2Byte(header, offset + NIOProtocolDecoder.TEXT_BEGIN_INDEX);
	}

	public byte[] getBinary() {
		return binary;
	}

	public int getBinaryLength() {
		return binaryLength;
	}

	public Integer getFutureID() {
		if (futureID == null) {
			futureID = 0;
		}
		return futureID;
	}

	public String getFutureName() {
		return futureName;
	}

	public int getHashCode() {
		return hashCode;
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}

	public String getText() {
		return text;
	}

	public int getTextLength() {
		return textLength;
	}

	public BufferedOutputStream getWriteBinaryBuffer() {
		return writeBinaryBuffer;
	}

	public boolean hasBinary() {
		return binaryLength > 0;
	}

	public boolean isBroadcast() {
		return futureID.intValue() == 0;
	}

	public boolean isReceiveBroadcast() {
		return FrontContext.FRONT_RECEIVE_BROADCAST.equals(getFutureName());
	}

	public boolean read(IOSession session,ByteBuffer buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doHeaderComplete(session,buf);
		}

		if (!body_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doBodyComplete(session,buf);
		}

		return true;
	}

	public void release() {
		ReleaseUtil.release(buf);
	}

	public void setFutureID(Object futureID) {
		this.futureID = (Integer) futureID;
	}

	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

	public String toString() {
		return futureName + "@" + getText();
	}

	public IOWriteFuture translate(IOSession session) throws IOException {

		if (!translated) {
			translated = true;
			this.write(text);
			this.writeBinary(binary);
		}

		return session.getProtocolEncoder().encode(session, this);
	}

	public void writeBinary(byte b) {

		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}

		writeBinaryBuffer.write(b);
	}

	public void writeBinary(byte[] bytes) {
		if (bytes == null) {
			return;
		}
		writeBinary(bytes, 0, bytes.length);
	}

	public void writeBinary(byte[] bytes, int offset, int length) {

		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}

		writeBinaryBuffer.write(bytes, offset, length);
	}

}
