package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.BeatWriteFuture;

public class TouchDistantJob implements Runnable {

	private EndPointWriter	writer	= null;

	private BeatWriteFuture	beat		= null;

	public TouchDistantJob(EndPointWriter writer,Session session) {
		this.writer = writer;
		this.beat = new BeatWriteFuture(session);
	}

	public void run() {
		writer.offer(beat);
	}
}
