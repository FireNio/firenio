package com.gifisan.nio.component.future;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Session;

public class TextReadFuture extends AbstractReadFuture implements IOReadFuture {

	public TextReadFuture(TCPEndPoint endPoint,ByteBuffer textBuffer, Session session, String serviceName) {
		super(endPoint,textBuffer, session, serviceName);
	}

	public boolean read() throws IOException {
		ByteBuffer buffer = this.textBuffer;
		endPoint.read(buffer);
		return !buffer.hasRemaining();
	}

	public void catchException(IOException e) {
	}

	public void setIOEvent(OutputStream outputStream, IOEventHandle handle) {
	}

}
