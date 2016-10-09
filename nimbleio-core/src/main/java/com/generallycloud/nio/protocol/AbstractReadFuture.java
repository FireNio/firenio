package com.generallycloud.nio.protocol;

import java.nio.charset.Charset;

import com.generallycloud.nio.Encoding;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SocketChannel;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected BufferedOutputStream	writeBuffer		= new BufferedOutputStream();
	protected IOEventHandle			ioEventHandle		;
	protected boolean 			hasOutputStream	;
	protected SocketChannel			channel			;
	protected IOSession			session			;
	protected boolean				flushed			;
	
	public IOEventHandle getIOEventHandle() {
		return ioEventHandle;
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
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

	public boolean flushed() {
		return flushed;
	}

}
