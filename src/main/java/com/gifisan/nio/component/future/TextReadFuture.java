package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(EndPoint endPoint,ByteBuffer textBuffer, Session session, String serviceName) {
		super(endPoint,textBuffer, session, serviceName);
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
