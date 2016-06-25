package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOSession;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;

public abstract class AbstractReadFuture extends ReadFutureImpl implements IOReadFuture, ReadFuture {

	protected TCPEndPoint		endPoint			= null;
	protected IOSession		session			= null;
	protected ByteBuffer		textBuffer		= null;
	protected boolean			hasStream			= false;
	private boolean			flushed			= false;
	private BufferedOutputStream	textCache			= new BufferedOutputStream();
	private ByteBuffer			header			= null;
	private boolean			headerComplete		= false;
	private boolean			textBufferComplete	= false;
	private int				textLength		= 0;
	private IOEventHandle		ioEventHandle		= null;

	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public AbstractReadFuture(Session session, Integer futureID, String serviceName) {
		this.session = (IOSession) session;
		this.endPoint = this.session.getTCPEndPoint();
		this.serviceName = serviceName;
		this.futureID = futureID;
	}

	public AbstractReadFuture(Session session, ByteBuffer header) {
		this.session = (IOSession) session;
		this.endPoint = this.session.getTCPEndPoint();
		this.header = header;
		if (header.position() == ProtocolDecoder.PROTOCOL_HADER) {
			doHeaderComplete(header);
		}
	}

	public TCPEndPoint getEndPoint() {
		return endPoint;
	}

	public Session getSession() {
		return session;
	}

	private void doHeaderComplete(ByteBuffer header) {

		headerComplete = true;

		byte[] header_array = header.array();

		int textAndServiceNameLength = gainTextLength(header_array)
				+ header_array[ProtocolDecoder.SERVICE_NAME_LENGTH_INDEX];

		this.futureID = gainFutureIDLength(header_array);

		this.textBuffer = ByteBuffer.allocate(textAndServiceNameLength);

		this.decode(endPoint, header.array());
	}

	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		if (!headerComplete) {

			ByteBuffer header = this.header;

			endPoint.read(header);

			if (header.hasRemaining()) {
				return false;
			}

			doHeaderComplete(header);
		}

		if (!textBufferComplete) {
			ByteBuffer buffer = this.textBuffer;

			endPoint.read(buffer);

			if (buffer.hasRemaining()) {
				return false;
			}

			textBufferComplete = true;

			this.gainServiceName(endPoint, header, buffer);

			this.gainText(endPoint, header, buffer);
		}

		return doRead(endPoint);
	}

	protected abstract boolean doRead(TCPEndPoint endPoint) throws IOException;

	protected void decode(TCPEndPoint endPoint, byte[] header) {

	}

	protected int gainStreamLength(byte[] header) {
		return MathUtil.byte2Int(header, ProtocolDecoder.STREAM_BEGIN_INDEX);
	}

	protected int gainTextLength(byte[] header) {
		int v0 = (header[5] & 0xff);
		int v1 = (header[6] & 0xff) << 8;
		int v2 = (header[7] & 0xff) << 16;

		this.textLength = v0 | v1 | v2;

		return textLength;
	}

	protected int gainFutureIDLength(byte[] header) {
		int v0 = (header[1] & 0xff);
		int v1 = (header[2] & 0xff) << 8;
		int v2 = (header[3] & 0xff) << 16;

		return v0 | v1 | v2;
	}

	protected void gainServiceName(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {

		byte[] bytes = buffer.array();

		this.serviceName = new String(bytes, 0, header.array()[ProtocolDecoder.SERVICE_NAME_LENGTH_INDEX]);
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {

		this.text = new String(buffer.array(), header.array()[ProtocolDecoder.SERVICE_NAME_LENGTH_INDEX], textLength,
				endPoint.getContext().getEncoding());
	}

	public boolean hasOutputStream() {
		return hasStream;
	}

	public boolean flushed() {
		return flushed;
	}

	public void flush() {
		endPoint.incrementWriter();
		flushed = true;
	}

	public void write(byte b) {
		textCache.write(b);
	}

	public void write(byte[] bytes) {
		textCache.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) {
		textCache.write(bytes, offset, length);
	}

	public void write(String content) {
		write(content, Encoding.DEFAULT);
	}

	public void write(String content, Charset encoding) {
		if (content == null) {
			return;
		}
		byte[] bytes = content.getBytes(encoding);
		textCache.write(bytes);
	}

	public BufferedOutputStream getTextCache() {
		return textCache;
	}

	public String toString() {
		return serviceName + "@" + getText();
	}
}
