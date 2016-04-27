package com.gifisan.nio.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.AbstractWriteFuture;
import com.gifisan.nio.component.future.WriteFuture;

public class BeatWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public BeatWriteFuture(EndPoint endPoint,Session session) {
		super(endPoint,null, null,null, null, session);
	}

	private ByteBuffer	beat	= ByteBuffer.wrap(new byte[] { 3 });

	public boolean write() throws IOException {
		beat.flip();
		attackNetwork(endPoint.write(beat));
		return !beat.hasRemaining();
	}
}
