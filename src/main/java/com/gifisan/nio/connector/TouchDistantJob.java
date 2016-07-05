package com.gifisan.nio.connector;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.future.BeatWriteFuture;

public class TouchDistantJob implements Runnable {

	private EndPointWriter	writer;
	private TCPEndPoint		endPoint;

	public TouchDistantJob(EndPointWriter writer, TCPEndPoint endPoint) {
		this.writer = writer;
		this.endPoint = endPoint;
	}

	public void run() {
		writer.offer(new BeatWriteFuture(endPoint));
	}
}
