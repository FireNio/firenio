package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.TCPEndPoint;

public class BeatWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public BeatWriteFuture(TCPEndPoint endPoint) {
		super(endPoint, 0, null, null, null);
	}

	private ByteBuffer	beat	= ByteBuffer.wrap(new byte[] { 3 });

	public boolean write() throws IOException {
		beat.flip();
		attackNetwork(endPoint.write(beat));
		return !beat.hasRemaining();
	}
}
