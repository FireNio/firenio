package com.gifisan.nio.component.protocol.nio.future;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.protocol.future.AbstractWriteFuture;
import com.gifisan.nio.component.protocol.future.WriteFuture;

public class BeatWriteFuture extends AbstractWriteFuture implements WriteFuture {

	public BeatWriteFuture(TCPEndPoint endPoint) {
		super(endPoint, null, null);
	}

	private static final Logger	logger	= LoggerFactory.getLogger(BeatWriteFuture.class);

	public void onException(IOException e) {
		logger.error(e.getMessage(), e);
	}

	public void onSuccess() {
	}

	public boolean write() throws IOException {

		ByteBuffer beat = ByteBuffer.wrap(new byte[] { 3 });

		beat.position(0);

		updateNetworkState(endPoint.write(beat));

		return !beat.hasRemaining();
	}
}
