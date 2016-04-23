package com.gifisan.nio.component;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(ByteBuffer textBuffer, Session session, String serviceName) {
		super(textBuffer, session, serviceName);
	}

	public boolean read() throws IOException {
		ByteBuffer buffer = this.textBuffer;
		endPoint.read(buffer);
		return !buffer.hasRemaining();
	}

	public void catchException(IOException e) {
	}

	public void setIOEvent(OutputStream outputStream, IOExceptionHandle handle) {
	}

}
