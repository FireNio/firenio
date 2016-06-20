package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPEndPoint;

public class TouchDistantJob implements Runnable {

	private EndPointWriter	writer	= null;

	private BeatWriteFuture	beat		= null;

	public TouchDistantJob(EndPointWriter writer,TCPEndPoint endPoint) {
		this.writer = writer;
		this.beat = new BeatWriteFuture(endPoint);
	}

	public void run() {
		writer.offer(beat);
	}
}
