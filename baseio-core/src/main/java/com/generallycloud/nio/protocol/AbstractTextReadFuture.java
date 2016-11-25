package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.SocketChannelContext;

public abstract class AbstractTextReadFuture extends AbstractChannelReadFuture implements TextReadFuture{

	protected AbstractTextReadFuture(SocketChannelContext context) {
		super(context);
	}

	protected String readText;
	
	protected StringBuilder writeTextBuffer = new StringBuilder();
	
	public StringBuilder getWriteTextBuffer() {
		return writeTextBuffer;
	}

	public void write(String text) {
		writeTextBuffer.append(text);
	}

	public String getReadText() {
		return readText;
	}

	public String getWriteText() {
		return writeTextBuffer.toString();
	}

	
	public void write(char c) {
		writeTextBuffer.append(c);
	}

	
	public void write(boolean b) {
		writeTextBuffer.append(b);
	}

	
	public void write(int i) {
		writeTextBuffer.append(i);
	}

	
	public void write(long l) {
		writeTextBuffer.append(l);
	}
	
	public void write(double d) {
		writeTextBuffer.append(d);
	}
	
}
