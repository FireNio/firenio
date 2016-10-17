package com.generallycloud.nio.protocol;

import java.nio.charset.Charset;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.NIOContext;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected BufferedOutputStream	writeBuffer	= new BufferedOutputStream();
	protected IOEventHandle			ioEventHandle;
	protected boolean				hasOutputStream;
	protected boolean				flushed;
	protected NIOContext			context;

	protected AbstractReadFuture(NIOContext context) {
		this.context = context;
	}

	public IOEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIOEventHandleAdaptor();
		}
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
		write(content, context.getEncoding());
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
