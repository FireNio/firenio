package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;

public abstract class AbstractReadFuture extends ReadFutureImpl implements IOReadFuture, ServerReadFuture {

	protected TCPEndPoint			endPoint		= null;
	protected Session			session		= null;
	protected ByteBuffer		textBuffer	= null;
	protected boolean			hasStream		= false;
	private boolean			flushed		= false;
	private BufferedOutputStream	textCache		= new BufferedOutputStream();

	public AbstractReadFuture(TCPEndPoint endPoint, ByteBuffer textBuffer, Session session, String serviceName) {
		super(serviceName);
		this.endPoint = endPoint;
		this.session = session;
		this.textBuffer = textBuffer;
	}

	public TCPEndPoint getEndPoint() {
		return endPoint;
	}

	public Session getSession() {
		return session;
	}

	public void catchOutputException(IOException e) {
		if (outputIOHandle != null) {
			outputIOHandle.handle(session, this, e);
		}
	}

	public void catchInputException(IOException e) {
		if (inputIOHandle != null) {
			inputIOHandle.handle(session, this, e);
		}
	}

	public IOEventHandle getInputIOHandle() {
		return inputIOHandle;
	}

	public IOEventHandle getOutputIOHandle() {
		return outputIOHandle;
	}

	public boolean hasOutputStream() {
		return hasStream;
	}

	public String getText() {
		if (text == null) {
			text = new String(textBuffer.array(), 0, textBuffer.position(), session.getContext().getEncoding());
		}
		return text;
	}

	public boolean flushed() {
		return flushed;
	}

	public void flush() {
		endPoint.incrementWriter();
		flushed = true;
	}

	public void write(byte b) throws IOException {
		textCache.write(b);
	}

	public void write(byte[] bytes) throws IOException {
		textCache.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) throws IOException {
		textCache.write(bytes, offset, length);
	}

	public void write(String content) {
		byte[] bytes = content.getBytes(Encoding.DEFAULT);
		textCache.write(bytes);
	}

	public void write(String content, Charset encoding) {
		byte[] bytes = content.getBytes(encoding);
		textCache.write(bytes);
	}

	public BufferedOutputStream getTextCache() {
		return textCache;
	}
	
}
