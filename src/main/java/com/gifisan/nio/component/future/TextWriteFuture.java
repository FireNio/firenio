package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.TCPEndPoint;

public class TextWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public TextWriteFuture(TCPEndPoint endPoint,Integer futureID, String serviceName,
			ByteBuffer textBuffer, byte[] textCache, IOEventHandle handle) {
		super(endPoint, handle, futureID, serviceName, textBuffer, textCache);
	}

	public boolean write() throws IOException {

		ByteBuffer buffer = this.textBuffer;
		// if (buffer.hasRemaining()) {
		attackNetwork(endPoint.write(buffer));

		return !buffer.hasRemaining();
		// }
		// return true;

	}
}
