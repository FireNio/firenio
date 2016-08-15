package com.gifisan.nio.component.protocol.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;

public class TextWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public TextWriteFuture(TCPEndPoint endPoint, ReadFuture readFuture, ByteBuffer textBuffer) {
		super(endPoint, readFuture, textBuffer);
	}

	public boolean write() throws IOException {

		ByteBuffer buffer = this.textBuffer;

		updateNetworkState(endPoint.write(buffer));

		return !buffer.hasRemaining();
	}
}
