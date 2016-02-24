package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.NIOSession;

public class NIOServletResponse implements InnerResponse {

	private static final Logger	logger			= LoggerFactory.getLogger(NIOServletResponse.class);
	private byte				RESPONSE_STREAM	= 1;
	private byte				RESPONSE_TEXT		= 0;
	private byte				emptyByte			= ' ';
	private int				dataLength		= 0;
	private ServerEndPoint		endPoint			= null;
	private boolean			flushed			= false;
	private byte				type				= RESPONSE_TEXT;
	private boolean			typed			= false;
	private BufferedOutputStream	bufferWriter		= new BufferedOutputStream();
	private OutputStream		writer			= null;
	private NIOSession			session			= null;
	private byte[]			header			= new byte[6];

	public NIOServletResponse(ServerEndPoint endPoint, NIOSession session) {
		this.endPoint = endPoint;
		this.session = session;
	}

	public void flush() throws IOException {
		if (type < RESPONSE_STREAM) {
			this.flushText();
		}
	}

	public void flushEmpty() throws IOException {
		this.endPoint.write(emptyByte);
		this.flushText();

	}

	private void flushText() throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (bufferWriter.size() == 0) {
			throw new NIOException("empty byte");
		}

		if (!endPoint.isOpened()) {
			throw new NIOException("channel closed");
		}

		this.flushed = true;

		ByteBuffer buffer = getByteBufferTEXT();

		this.bufferWriter.reset();
		this.endPoint.write(buffer);

	}

	private ByteBuffer getByteBufferStream() {
		byte[] header = this.header;
		int _dataLength = dataLength;

		header[0] = type;
		header[1] = 0;
		header[2] = (byte) (_dataLength & 0xff);
		header[3] = (byte) ((_dataLength >> 8) & 0xff);
		header[4] = (byte) ((_dataLength >> 16) & 0xff);
		header[5] = (byte) (_dataLength >>> 24);

		return ByteBuffer.wrap(header);
	}

	private ByteBuffer getByteBufferTEXT() {
		int length = bufferWriter.size();
		byte[] header = this.header;

		header[0] = type;
		header[1] = session.getSessionID();
		header[2] = (byte) (length & 0xff);
		header[3] = (byte) ((length >> 8) & 0xff);
		header[4] = (byte) ((length >> 16) & 0xff);
		header[5] = (byte) (length >>> 24);

		ByteBuffer buffer = ByteBuffer.allocate(length + 6);

		buffer.put(header);
		buffer.put(bufferWriter.toByteArray());
		buffer.flip();

		return buffer;
	}

	public void setStreamResponse(int length) throws IOException {
		if (length < 1) {
			throw new IOException("invalidate length");
		}

		if (typed) {
			throw new IOException("response typed");
		}

		this.type = RESPONSE_STREAM;
		this.dataLength = length;

		ByteBuffer buffer = getByteBufferStream();

		this.typed = true;
		this.flushed = true;
		this.endPoint.write(buffer);
		this.writer = this.endPoint;
	}

	public void write(byte b) throws IOException {
		this.writer.write(b);

	}

	public void write(byte[] bytes) throws IOException {
		this.writer.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		this.writer.write(bytes, offset, length);

	}

	public void write(String content) {
		try {
			byte[] bytes = content.getBytes(Encoding.DEFAULT);
			writer.write(bytes);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

	public Response update() {
		this.type = RESPONSE_TEXT;
		this.writer = this.bufferWriter;
		this.flushed = false;
		this.typed = false;
		return this;
	}

	public boolean flushed() {
		return flushed;
	}

	public void write(String content, Charset encoding) {
		try {
			byte[] bytes = content.getBytes(encoding);
			writer.write(bytes);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}
	}

}
