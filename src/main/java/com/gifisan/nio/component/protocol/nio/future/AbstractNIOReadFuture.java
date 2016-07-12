package com.gifisan.nio.component.protocol.nio.future;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.AbstractReadFuture;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public abstract class AbstractNIOReadFuture extends AbstractReadFuture implements IOReadFuture ,NIOReadFuture{

	protected ByteBuffer		textBuffer		;
	protected boolean			hasStream			;
	private ByteBuffer			header			;
	private boolean			headerComplete		;
	private boolean			textBufferComplete	;
	private int				textLength		;
	private Parameters			parameters	;
	protected OutputStream		outputStream	;
	protected InputStream		inputStream	;
	protected String	serviceName	;
	protected String	text			;
	protected Integer	futureID		;
	
	public String getServiceName() {
		return serviceName;
	}

	public String getText() {
		return text;
	}
	
	public Integer getFutureID() {
		return futureID;
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public int getStreamLength() {
		return 0;
	}

	public AbstractNIOReadFuture(Session session, Integer futureID, String serviceName) {
		super(session);
		this.serviceName = serviceName;
		this.futureID = futureID;
	}

	public AbstractNIOReadFuture(Session session, ByteBuffer header) {
		super(session);
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

		int text_and_service_name_length = gainTextLength(header_array)
				+ header_array[ProtocolDecoder.SERVICE_NAME_LENGTH_INDEX];

		this.futureID = gainFutureIDLength(header_array);

		this.textBuffer = ByteBuffer.allocate(text_and_service_name_length);

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

	public String toString() {
		return serviceName + "@" + getText();
	}
}
