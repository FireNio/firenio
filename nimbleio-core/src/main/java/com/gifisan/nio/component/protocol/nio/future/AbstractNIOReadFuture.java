package com.gifisan.nio.component.protocol.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.MathUtil;
import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.AbstractIOReadFuture;
import com.gifisan.nio.component.protocol.nio.NIOProtocolDecoder;

public abstract class AbstractNIOReadFuture extends AbstractIOReadFuture implements NIOReadFuture {

	private int			text_length;
	private int			service_name_length;
	private boolean		header_complete;
	private boolean		text_buffer_complete;
	private ByteBuffer		header;
	private Parameters		parameters;
	protected boolean		hasStream;
	protected String		serviceName;
	protected ByteBuffer	textBuffer;
	protected String		text;
	protected Integer		futureID;

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

	public int getStreamLength() {
		return 0;
	}

	public AbstractNIOReadFuture(Session session, Integer futureID, String serviceName) {
		super(session);
		this.serviceName = serviceName;
		this.futureID = futureID;
	}

	public AbstractNIOReadFuture(Session session, boolean isBeatPacket) {
		super(session);
		this.isBeatPacket = isBeatPacket;
	}

	public AbstractNIOReadFuture(Session session, ByteBuffer header) {
		super(session);
		this.header = header;
		if (header.position() == NIOProtocolDecoder.PROTOCOL_HADER) {
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

		header_complete = true;

		byte[] header_array = header.array();

		this.service_name_length = (header_array[0] & 0x3f);

		int text_and_service_name_length = gainTextLength(header_array) + service_name_length;

		this.futureID = gainFutureID(header_array);

		this.textBuffer = ByteBuffer.allocate(text_and_service_name_length);

		this.decode(endPoint, header.array());
	}

	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		if (!header_complete) {

			ByteBuffer header = this.header;

			endPoint.read(header);

			if (header.hasRemaining()) {
				return false;
			}

			doHeaderComplete(header);
		}

		if (!text_buffer_complete) {
			ByteBuffer buffer = this.textBuffer;

			endPoint.read(buffer);

			if (buffer.hasRemaining()) {
				return false;
			}

			text_buffer_complete = true;

			this.gainServiceName(endPoint, header, buffer);

			this.gainText(endPoint, header, buffer);
		}

		return doRead(endPoint);
	}

	protected abstract boolean doRead(TCPEndPoint endPoint) throws IOException;

	protected void decode(TCPEndPoint endPoint, byte[] header) {

	}

	protected int gainStreamLength(byte[] header) {
		return MathUtil.byte2Int(header, NIOProtocolDecoder.STREAM_BEGIN_INDEX);
	}

	protected int gainTextLength(byte[] header) {
		int v0 = (header[4] & 0xff);
		int v1 = (header[5] & 0xff) << 8;
		int v2 = (header[6] & 0xff) << 16;

		this.text_length = v0 | v1 | v2;

		return text_length;
	}

	protected int gainFutureID(byte[] header) {
		int v0 = (header[1] & 0xff);
		int v1 = (header[2] & 0xff) << 8;
		int v2 = (header[3] & 0xff) << 16;

		return v0 | v1 | v2;
	}

	protected void gainServiceName(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {

		byte[] bytes = buffer.array();

		this.serviceName = new String(bytes, 0, service_name_length);
	}

	protected void gainText(TCPEndPoint endPoint, ByteBuffer header, ByteBuffer buffer) throws IOException {

		this.text = new String(buffer.array(), service_name_length, text_length, endPoint.getContext().getEncoding());
	}

	//FIXME check
	public boolean hasOutputStream() {
		return hasStream;
	}

	public String toString() {
		return serviceName + "@" + getText();
	}
}
