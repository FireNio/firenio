package com.gifisan.nio.component.protocol;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOSession;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	private BufferedOutputStream	writeBuffer		= new BufferedOutputStream();
	private IOEventHandle		ioEventHandle		;
	protected boolean 		hasOutputStream	;
	protected boolean			flushed			;
	protected InputStream		inputStream;
	protected OutputStream		outputStream;
	protected TCPEndPoint		endPoint			;
	protected IOSession		session			;
	
	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}
	
	public AbstractReadFuture(Session session) {
		this.session = (IOSession) session;
		this.endPoint = this.session.getTCPEndPoint();
	}
	
	public boolean flushed() {
		return flushed;
	}

	public void write(byte b) {
		writeBuffer.write(b);
	}

	public void write(byte[] bytes) {
		writeBuffer.write(bytes);
	}

	public void write(byte[] bytes, int offset, int length) {
		writeBuffer.write(bytes, offset, length);
	}

	public void write(String content) {
		write(content, Encoding.DEFAULT);
	}

	public void write(String content, Charset encoding) {
		if (content == null) {
			return;
		}
		byte[] bytes = content.getBytes(encoding);
		writeBuffer.write(bytes);
	}

	public BufferedOutputStream getWriteBuffer() {
		return writeBuffer;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public boolean hasOutputStream() {
		return hasOutputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
}
