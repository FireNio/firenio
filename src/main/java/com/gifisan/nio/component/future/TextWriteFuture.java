package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.Session;

public class TextWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public TextWriteFuture(EndPoint endPoint, Session session, String serviceName, ByteBuffer textBuffer,
			byte[] textCache, IOExceptionHandle handle) {
		super(endPoint, handle, serviceName, textBuffer, textCache, session);
	}

	public boolean write() throws IOException {

		ByteBuffer buffer = this.textBuffer;
		if (buffer.hasRemaining()) {
			attackNetwork(endPoint.write(buffer));

			return !buffer.hasRemaining();
		}
		return true;

	}
}
