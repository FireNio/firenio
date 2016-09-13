package com.generallycloud.nio.component.protocol.nio.future;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.DefaultParameters;
import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.TCPEndPoint;
import com.generallycloud.nio.component.protocol.AbstractIOReadFuture;

/**
 * @author wangkai
 *
 */
public class NIOReadFutureImpl extends AbstractIOReadFuture implements NIOReadFuture {

	private int				textLength;
	private int				service_name_length;
	private boolean			header_complete;
	private boolean			body_complete;
	private Parameters			parameters;
	private ByteBuf			buffer;
	private String				serviceName;
	private String				text;
	private Integer			futureID;
	private int				binaryLength;
	private BufferedOutputStream	writeBinaryBuffer;
	private byte []			binary;

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

	public void release() {
		ReleaseUtil.release(buffer);
	}
	
	public boolean hasBinary() {
		return binaryLength > 0;
	}

	public int getTextLength() {
		return textLength;
	}

	public int getBinaryLength() {
		return binaryLength;
	}

	public NIOReadFutureImpl(Session session, Integer futureID, String serviceName) {
		super(session);
		this.serviceName = serviceName;
		this.futureID = futureID;
	}

	public NIOReadFutureImpl(Session session, boolean isBeatPacket) {
		super(session);
		this.isBeatPacket = isBeatPacket;
	}

	public NIOReadFutureImpl(Session session, ByteBuf buffer) throws IOException {
		super(session);
		this.buffer = buffer;
		if (!buffer.hasRemaining()) {
			doHeaderComplete(buffer);
		}
	}

	private void doHeaderComplete(ByteBuf buffer) throws IOException {

		header_complete = true;

		byte[] header_array = buffer.array();

		int offset = buffer.offset();

		int service_name_length = (header_array[offset] & 0x3f);

		int textLength = gainTextLength(header_array, offset);

		int binaryLength = gainBinaryLength(header_array, offset);

		int all_length = service_name_length + textLength + binaryLength;
		
		this.service_name_length = service_name_length;

		this.textLength = textLength;

		this.binaryLength = binaryLength;

		this.futureID = gainFutureID(header_array);

		if (buffer.capacity() >= all_length) {

			buffer.limit(all_length);

		} else {

			ReleaseUtil.release(buffer);

			if (all_length > 1024 * 10) {
				throw new IOException("max length 1024 * 10,length=" + all_length);
			}

			this.buffer = endPoint.getContext().getDirectByteBufferPool().allocate(all_length);
		}
	}

	public boolean read() throws IOException {

		TCPEndPoint endPoint = this.endPoint;

		ByteBuf buffer = this.buffer;

		if (!header_complete) {

			buffer.read(endPoint);

			if (buffer.hasRemaining()) {
				return false;
			}

			doHeaderComplete(buffer);
		}

		if (!body_complete) {

			buffer.read(endPoint);

			if (buffer.hasRemaining()) {
				return false;
			}
			
			doBodyComplete(buffer);
		}

		return true;
	}

	private void doBodyComplete(ByteBuf buffer) {

		body_complete = true;

		byte[] array = buffer.array();

		int offset = buffer.offset();

		this.gainServiceName(array, offset);

		this.gainText(array, offset);
		
		this.gainBinary(array, offset);
	}

	private int gainBinaryLength(byte[] header, int offset) {
		int v0 = (header[offset + 8] & 0xff);
		int v1 = (header[offset + 7] & 0xff) << 8;
		int v2 = (header[offset + 6] & 0xff) << 16;

		return v0 | v1 | v2;
	}

	private int gainTextLength(byte[] header, int offset) {
		int v0 = (header[offset + 5] & 0xff);
		int v1 = (header[offset + 4] & 0xff) << 8;
		return v0 | v1;
	}

	private int gainFutureID(byte[] header) {
		int v0 = (header[3] & 0xff);
		int v1 = (header[2] & 0xff) << 8;
		int v2 = (header[1] & 0xff) << 16;

		return v0 | v1 | v2;
	}

	private void gainServiceName(byte[] array, int offset) {

		this.serviceName = new String(array, offset, service_name_length);
	}

	private void gainText(byte[] array, int offset) {

		this.text = new String(array, offset + service_name_length, textLength,endPoint.getContext().getEncoding());
	}
	
	private void gainBinary(byte[] array, int offset) {
		
		this.binary = new byte[binaryLength]; 
				
		System.arraycopy(array, offset + buffer.limit() - binaryLength, binary, 0, binaryLength);
	}

	public String toString() {
		return serviceName + "@" + getText();
	}

	public BufferedOutputStream getWriteBinaryBuffer() {
		return writeBinaryBuffer;
	}

	public void writeBinary(byte b) {
		
		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}
		
		writeBinaryBuffer.write(b);
	}
	
	public byte[] getBinary() {
		return binary;
	}

	public void writeBinary(byte[] bytes) {
		
		writeBinary(bytes, 0, bytes.length);
	}

	public void writeBinary(byte[] bytes, int offset, int length) {
		
		if (writeBinaryBuffer == null) {
			writeBinaryBuffer = new BufferedOutputStream();
		}
		
		writeBinaryBuffer.write(bytes, offset, length);
	}
	
}
