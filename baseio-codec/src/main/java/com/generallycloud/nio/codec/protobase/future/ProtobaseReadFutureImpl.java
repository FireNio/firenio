package com.generallycloud.nio.codec.protobase.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.generallycloud.nio.balance.AbstractBalanceReadFuture;
import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.JsonParameters;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketSession;

/**
 *
 */
public class ProtobaseReadFutureImpl extends AbstractBalanceReadFuture implements ProtobaseReadFuture {

	private byte[]				binary;
	private int				binaryLength;
	private int				binaryLimit;
	private boolean			body_complete;
	private ByteBuf			buf;
	private Integer			futureID;
	private String				futureName;
	private int				hashCode;
	private boolean			header_complete;
	private Parameters			parameters;
	protected String			readText;
	private int				service_name_length;
	private int				textLength;
	private boolean			translated;

	private BufferedOutputStream	writeBinaryBuffer;

	protected StringBuilder		writeTextBuffer	= new StringBuilder();

	// for ping & pong
	public ProtobaseReadFutureImpl(SocketChannelContext context) {
		super(context);
	}

	public ProtobaseReadFutureImpl(SocketChannelContext context, Integer futureID, String futureName) {
		super(context);
		this.futureName = futureName;
		this.futureID = futureID;
	}

	public ProtobaseReadFutureImpl(SocketChannelContext context, String futureName) {
		super(context);
		this.futureName = futureName;
	}

	public ProtobaseReadFutureImpl(SocketSession session, ByteBuf buf) throws IOException {
		this(session, buf, 1024 * 1024 * 2);
	}

	public ProtobaseReadFutureImpl(SocketSession session, ByteBuf buf, int binaryLimit) throws IOException {
		super(session.getContext());
		this.buf = buf;
		this.binaryLimit = binaryLimit;
		if (!buf.hasRemaining()) {
			doHeaderComplete(session, buf);
		}
	}

	private void doBodyComplete(Session session, ByteBuf buf) {

		body_complete = true;

		buf.flip();

		Charset charset = session.getEncoding();

		int offset = buf.offset();

		ByteBuffer memory = buf.nioBuffer();

		memory.limit(offset + service_name_length);

		futureName = StringUtil.decode(charset, memory);

		memory.limit(memory.position() + textLength);

		readText = StringUtil.decode(charset, memory);

		this.gainBinary(buf, offset);
	}

	private void doHeaderComplete(Session session, ByteBuf buf) throws IOException {

		header_complete = true;
		
		buf.flip();
		
		buf.skipBytes(1);

		this.service_name_length = buf.getUnsignedByte();

		this.futureID = buf.getInt();

		this.sessionID = buf.getInt();

		this.hashCode = buf.getInt();

		this.textLength = buf.getUnsignedShort();

		this.binaryLength = buf.getInt();

		if (binaryLength > binaryLimit) {

			throw new IOException("max length " + binaryLimit + ",length=" + binaryLength);
		}

		int all_length = service_name_length + textLength + binaryLength;

		if (buf.capacity() >= all_length) {

			buf.limit(all_length);

		} else {

			ReleaseUtil.release(buf);

			this.buf = allocate(session,all_length);
		}
	}

	private void gainBinary(ByteBuf buf, int offset) {

		if (binaryLength < 1) {
			return;
		}

		buf.skipBytes(service_name_length + textLength);

		binary = new byte[binaryLength];

		buf.get(binary);
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
			parameters = new JsonParameters(getText());
		}
		return parameters;
	}

	public String getReadText() {
		return readText;
	}

	public String getText() {
		return readText;
	}

	public int getTextLength() {
		return textLength;
	}

	public BufferedOutputStream getWriteBinaryBuffer() {
		return writeBinaryBuffer;
	}

	public String getWriteText() {
		return writeTextBuffer.toString();
	}

	public StringBuilder getWriteTextBuffer() {
		return writeTextBuffer;
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

	public boolean read(SocketSession session, ByteBuf buffer) throws IOException {

		ByteBuf buf = this.buf;

		if (!header_complete) {

			buf.read(buffer);

			if (buf.hasRemaining()) {
				return false;
			}

			doHeaderComplete(session, buf);
		}

		if (!body_complete) {

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

	public void setFutureID(Object futureID) {
		this.futureID = (Integer) futureID;
	}

	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

	public String toString() {
		return futureName + "@" + getText();
	}

	public BalanceReadFuture translate(){

		if (!translated) {
			translated = true;
			this.write(readText);
			this.writeBinary(binary);
		}

		return this;
	}

	public void write(boolean b) {
		writeTextBuffer.append(b);
	}

	public void write(char c) {
		writeTextBuffer.append(c);
	}

	public void write(double d) {
		writeTextBuffer.append(d);
	}

	public void write(int i) {
		writeTextBuffer.append(i);
	}

	public void write(long l) {
		writeTextBuffer.append(l);
	}

	public void write(String text) {
		if (StringUtil.isNullOrBlank(text)) {
			return;
		}
		writeTextBuffer.append(text);
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
