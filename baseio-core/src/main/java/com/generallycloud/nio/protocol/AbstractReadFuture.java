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

	@Override
	public boolean flushed() {
		return flushed;
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	public IoEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIoEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	@Override
	public String getReadText() {
		return readText;
	}

	@Override
	public String getWriteText() {
		return writeTextBuffer.toString();
	}

	@Override
	public StringBuilder getWriteTextBuffer() {
		return writeTextBuffer;
	}

	@Override
	public void setIOEventHandle(IoEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	@Override
	public void write(boolean b) {
		writeTextBuffer.append(b);
	}

	@Override
	public void write(char c) {
		writeTextBuffer.append(c);
	}

	@Override
	public void write(double d) {
		writeTextBuffer.append(d);
	}

	@Override
	public void write(int i) {
		writeTextBuffer.append(i);
	}

	@Override
	public void write(long l) {
		writeTextBuffer.append(l);
	}

	@Override
	public void write(String text) {
		writeTextBuffer.append(text);
	}

}
