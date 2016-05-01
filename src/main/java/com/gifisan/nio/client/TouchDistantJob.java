package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter1;
import com.gifisan.nio.component.Session;

public class TouchDistantJob implements Runnable {

	private EndPointWriter1	writer	= null;

	private BeatWriteFuture	beat		= null;

	public TouchDistantJob(EndPointWriter1 writer,EndPoint endPoint,Session session) {
		this.writer = writer;
		this.beat = new BeatWriteFuture(endPoint,session);
	}

	public void run() {
		writer.offer(beat);
	}
}
