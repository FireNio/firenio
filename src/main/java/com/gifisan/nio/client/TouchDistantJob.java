package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.Session;

public class TouchDistantJob implements Runnable {

	private EndPointWriter	writer	= null;

	private BeatWriteFuture	beat		= null;

	public TouchDistantJob(EndPointWriter writer,EndPoint endPoint,Session session) {
		this.writer = writer;
		this.beat = new BeatWriteFuture(endPoint,session);
	}

	public void run() {
		writer.forceOffer(beat);
	}
}
