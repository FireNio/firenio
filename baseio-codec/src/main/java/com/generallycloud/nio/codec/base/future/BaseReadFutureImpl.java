package com.generallycloud.nio.codec.base.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.balance.AbstractBalanceReadFuture;
import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.codec.base.BaseProtocolDecoder;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.DefaultParameters;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.IOWriteFuture;

/**
 *
 */
public class BaseReadFutureImpl extends AbstractBalanceReadFuture implements BaseReadFuture {

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
	private int 				binaryLimit;
	private BufferedOutputStream	writeBinaryBuffer;

	// for ping & pong
	public BaseReadFutureImpl(BaseContext context) {
		super(context);
	}

	public BaseReadFutureImpl(BaseContext context,Integer futureID, String futureName) {
		super(context);
		this.futureName = futureName;
		this.futureID = futureID;
	}

	public BaseReadFutureImpl(IOSession session, ByteBuf buf) throws IOException {
		this(session, buf, 1024 * 1024 * 2);
	}
	
	public BaseReadFutureImpl(IOSession session, ByteBuf buf,int binaryLimit) throws IOException {
		super(session.getContext());
		this.buf = buf;
		this.binaryLimit = binaryLimit;
	}

	public BaseReadFutureImpl(BaseContext context,String futureName) {
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
		
		this.service_name_length = header_array[offset + 1] & 0xff;

		this.textLength = gainTextLength(header_array, offset);

		this.binaryLength = gainBinaryLength(header_array, offset);
		
		this.isBroadcast = gainBroadcast(header_array, offset);

		this.futureID = gainFutureID(header_array, offset);
		
		this.sessionID = gainSessionID(header_array, offset);
		
		this.hashCode = gainHashCode(header_array, offset);
		
		if (binaryLength > binaryLimit) {
			
			throw new IOException("max length "+binaryLimit+",length=" + binaryLength);
		}
		
		int all_length = service_name_length + textLength + binaryLength;

		if (buf.capacity() >= all_length) {

			buf.limit(all_length);

		} else {

			ReleaseUtil.release(buf);

			this.buf = allocate(all_length);
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
		return MathUtil.byte2Int(header, offset + BaseProtocolDecoder.BINARY_BEGIN_INDEX);
	}

	private int gainFutureID(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + BaseProtocolDecoder.FUTURE_ID_BEGIN_INDEX);
	}
	
	private int gainSessionID(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + BaseProtocolDecoder.SESSION_ID_BEGIN_INDEX);
	}
	
	private boolean gainBroadcast(byte[] header, int offset){
		return (header[offset] & 0x20) == 0x20;
	}
	
	private int gainHashCode(byte[] header, int offset) {
		return MathUtil.byte2Int(header, offset + BaseProtocolDecoder.HASH_BEGIN_INDEX);
	}

	private int gainTextLength(byte[] header, int offset) {
		return MathUtil.byte2IntFrom2Byte(header, offset + BaseProtocolDecoder.TEXT_BEGIN_INDEX);
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

	public IOWriteFuture translate() throws IOException {

		if (!translated) {
			translated = true;
			this.write(text);
			this.writeBinary(binary);
		}

		return context.getProtocolEncoder().encode(context, this);
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
