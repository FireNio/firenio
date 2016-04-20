package com.gifisan.nio.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.ProtocolEncoder;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.InnerSession;
import com.gifisan.nio.server.session.NIOSession;

public class ServiceResponse implements InnerResponse {

	private int				dataLength		= 0;
	private ServerEndPoint		endPoint			= null;
	private boolean			flushed			= false;
	private BufferedOutputStream	textBuffer		= new BufferedOutputStream();
	private NIOSession			session			= null;
	private boolean			scheduled			= false;
	private ProtocolEncoder		encoder			= null;
	private byte				sessionID			= 0;
	private CatchWriteException	catchWriteException	= null;
	private ByteBuffer			buffer			= null;
	private InputStream			inputStream		= null;
	private int				writedLength		= 0;
	private EndPointWriter		endPointWriter		= null;
	private ByteArrayInputStream bInputStream		= null;

	public ServiceResponse(ServerEndPoint endPoint, NIOSession session) {
		this.endPoint = endPoint;
		this.session = session;
		this.sessionID = session.getSessionID();
		this.encoder = session.getServerContext().getProtocolEncoder();
		this.endPointWriter = session.getServerContext().getEndPointWriter();
	}

	public void flush() throws IOException {
		if (flushed) {
			throw new FlushedException("flushed already");
		}

		if (!endPoint.isOpened()) {
			throw new IOException("channel closed");
		}

		this.flushed = true;

		this.scheduled = true;
		
		this.session.removeServerOutputStream();

		this.buffer = encoder.encode(sessionID, textBuffer.toByteArray(), dataLength);

		this.buffer.flip();

		if (dataLength > 0) {
			
			if (bInputStream == null) {
				
				MultiResponseWriter writer = new MultiResponseWriter(
						buffer, 
						endPoint, 
						sessionID, 
						session.getRequest(), 
						catchWriteException, 
						writedLength, 
						dataLength, 
						inputStream);
				this.endPointWriter.offer(writer);
				return;
			}
			
			BAISResponseWriter writer = new BAISResponseWriter(
					buffer, 
					endPoint, 
					sessionID, 
					session.getRequest(), 
					catchWriteException, 
					bInputStream);
			this.endPointWriter.offer(writer);
			return;
		}

		TextResponseWriter writer = new TextResponseWriter(
				buffer, 
				endPoint, 
				sessionID, 
				session.getRequest(), 
				catchWriteException);
		
		
		this.endPointWriter.offer(writer);
		
	}

	public void setInputStream(InputStream inputStream) throws IOException {
		this.dataLength = inputStream.available();
		if (inputStream.getClass() != ByteArrayInputStream.class) {
			this.inputStream = inputStream;
			return;
		}
		this.bInputStream = (ByteArrayInputStream) inputStream;
	}

	public void write(String content) {
		byte[] bytes = content.getBytes(Encoding.DEFAULT);
		textBuffer.write(bytes);
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

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public byte getSessionID() {
		return sessionID;
	}

	public boolean complete1() {
		return !buffer.hasRemaining() && writedLength == dataLength;
	}

	public void catchException(CatchWriteException catchWriteException) {
		this.catchWriteException = catchWriteException;
	}

	public InnerSession getInnerSession() {
		return session;
	}
}
