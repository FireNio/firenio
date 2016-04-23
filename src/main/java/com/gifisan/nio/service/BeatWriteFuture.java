package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.AbstractWriteFuture;
import com.gifisan.nio.server.session.Session;

public class BeatWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public BeatWriteFuture(Session session) {
		super(null, null, null, session);
	}

	private ByteBuffer	beat	= ByteBuffer.wrap(new byte[] { 3 });

	public boolean write() throws IOException {
		beat.flip();
		attackNetwork(endPoint.write(beat));
		return !beat.hasRemaining();
	}
}
