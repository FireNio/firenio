package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.MathUtil;
import com.generallycloud.nio.component.DefaultParameters;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.AbstractIOReadFuture;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolDecoder;

public abstract class AbstractNIOReadFuture extends AbstractIOReadFuture implements NIOReadFuture {

	private int			text_length;
	private int			service_name_length;
	private boolean		header_complete;
	private boolean		text_buffer_complete;
	private Parameters		parameters;
	protected ByteBuf		buffer;
	protected boolean		hasStream;
	protected String		serviceName;
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

	public AbstractNIOReadFuture(Session session, ByteBuf buffer) throws IOException {
		super(session);
		this.buffer = buffer;
		if (!buffer.hasRemaining()) {
			doHeaderComplete(buffer);
		}
	}

	public TCPEndPoint getEndPoint() {
		return endPoint;
	}

	public Session getSession() {
		return session;
	}

	private void doHeaderComplete(ByteBuf buffer) throws IOException {

		header_complete = true;

		buffer.flip();
		
		byte [] header_array = buffer.getBytes();

		this.service_name_length = (header_array[0] & 0x3f);

		int text_and_service_name_length = gainTextLength(header_array) + service_name_length;

		this.futureID = gainFutureID(header_array);
		
		if (buffer.capacity() >= text_and_service_name_length) {
			
			buffer.clear().limit(text_and_service_name_length);
			
		}else{

			buffer.release();
			
			if (text_and_service_name_length > 1024 * 10) {
				throw new IOException("max length 1024 * 10,length="+text_and_service_name_length);
			}
			
			this.buffer = endPoint.getContext().getDirectByteBufferPool().allocate(text_and_service_name_length);
		}
		
		this.decode(endPoint, header_array);
	}

	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		if (!header_complete) {

			ByteBuf buffer = this.buffer;

			buffer.read(endPoint);

			if (buffer.hasRemaining()) {
				return false;
			}

			doHeaderComplete(buffer);
		}

		if (!text_buffer_complete) {
			ByteBuf buffer = this.buffer;

			buffer.read(endPoint);

			if (buffer.hasRemaining()) {
				return false;
			}

			text_buffer_complete = true;
			
			buffer.flip();
			
			byte [] array = buffer.getBytes();

			this.gainServiceName(endPoint, array);

			this.gainText(endPoint, array);
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

	protected void gainServiceName(TCPEndPoint endPoint,byte [] array) throws IOException {

		this.serviceName = new String(array, 0, service_name_length);
	}

	protected void gainText(TCPEndPoint endPoint,byte [] array) throws IOException {

		this.text = new String(array, service_name_length, text_length, endPoint.getContext().getEncoding());
	}

	//FIXME check
	public boolean hasOutputStream() {
		return hasStream;
	}

	public String toString() {
		return serviceName + "@" + getText();
	}
}
