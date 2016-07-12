package com.gifisan.nio.component.protocol.future;

import java.nio.charset.Charset;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.IOSession;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.TCPEndPoint;

public abstract class AbstractReadFuture extends FutureImpl implements IOReadFuture {

	protected TCPEndPoint		endPoint			;
	protected IOSession		session			;
	private boolean			flushed			;
	private BufferedOutputStream	writeBuffer		= new BufferedOutputStream();
	private IOEventHandle		ioEventHandle		;
	
	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}
	
	//FIXME 4test
	protected AbstractReadFuture() {
		new Exception("4 test").printStackTrace();
	}

	public AbstractReadFuture(Session session) {
		this.session = (IOSession) session;
		this.endPoint = this.session.getTCPEndPoint();
	}

	public boolean flushed() {
		return flushed;
	}

	public void flush() {
		endPoint.incrementWriter();
		flushed = true;
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

}
