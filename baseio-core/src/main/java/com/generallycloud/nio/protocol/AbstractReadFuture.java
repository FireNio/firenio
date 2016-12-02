package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketChannelContext;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected boolean				flushed;
	protected String				readText;
	protected SocketChannelContext	context;
	protected IoEventHandle			ioEventHandle;
	protected StringBuilder			writeTextBuffer = new StringBuilder();

	protected AbstractReadFuture(SocketChannelContext context) {
		this.context = context;
	}

	public boolean flushed() {
		return flushed;
	}

	public SocketChannelContext getContext() {
		return context;
	}

	public IoEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIoEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	public String getReadText() {
		return readText;
	}

	public String getWriteText() {
		return writeTextBuffer.toString();
	}

	public StringBuilder getWriteTextBuffer() {
		return writeTextBuffer;
	}

	public void setIOEventHandle(IoEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public void write(boolean b) {
		writeTextBuffer.append(b);
	}

	public void write(char c) {
		writeTextBuffer.append(c);
	}

	public void write(double d) {
		writeTextBuffer.append(d);
	}

	public void write(int i) {
		writeTextBuffer.append(i);
	}

	public void write(long l) {
		writeTextBuffer.append(l);
	}

	public void write(String text) {
		writeTextBuffer.append(text);
	}

}
