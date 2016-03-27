package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.NIOSession;

public class ServiceResponse implements InnerResponse {

	private int				dataLength	= 0;
	private ServerEndPoint		endPoint		= null;
	private boolean			flushed		= false;
	private BufferedOutputStream	textBuffer	= new BufferedOutputStream();
	private NIOSession			session		= null;
	private boolean			scheduled		= false;
	private ProtocolEncoder		encoder		= null;
	private byte				sessionID		= 0;
	private OutputStream		outputStream	= null;

	public ServiceResponse(ServerEndPoint endPoint, NIOSession session) {
		this.endPoint = endPoint;
		this.session = session;
		this.sessionID = session.getSessionID();
		this.encoder = session.getServerContext().getProtocolEncoder();
	}

	public void flush() throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new NIOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;

		ByteBuffer buffer = encoder.encode(sessionID, textBuffer.toByteArray(), dataLength);
		
		textBuffer.reset();

		buffer.flip();

		this.endPoint.completedWrite(buffer);
	}

	public void setStream(int length) throws IOException {
		if (length < 1) {
			throw new IOException("invalidate length:" + length);
		}

		this.dataLength = length;
	}

	public void write(String content) {
		byte[] bytes = content.getBytes(Encoding.DEFAULT);
		textBuffer.write(bytes);
	}

	public Response update() {
		this.flushed = false;
		this.scheduled = false;
		this.dataLength = 0;
		return this;
	}

	public boolean flushed() {
		return flushed;
	}

	public void schdule() {
		this.scheduled = true;
	}

	public boolean schduled() {
		return scheduled;
	}
	
	public void write(String content, Charset encoding) {
		byte[] bytes = content.getBytes(encoding);
		textBuffer.write(bytes);
	}

	public void write(byte b) throws IOException {
		textBuffer.write(b);
	}

	public void write(byte[] bytes) throws IOException {
		textBuffer.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		textBuffer.write(bytes, offset, length);
	}

	public OutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = new EndPointOutputStream(endPoint);
		}
		return outputStream;
	}

}
