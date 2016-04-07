package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.server.InnerResponse;

public class EndPointWriter extends AbstractLifeCycle implements Runnable {

	private Thread						owner	= null;
	private boolean					running	= false;
//	private BlockingQueue<InnerResponse>	writers	= new ArrayBlockingQueue<InnerResponse>(1024 * 5000);
	private LinkedList<InnerResponse>		writers	= new LinkedList<InnerResponse>();

	public void offer(InnerResponse response) {
		this.writers.offer(response);
	}

	public void run() {

//		BlockingQueue<InnerResponse> writers = this.writers;
		LinkedList<InnerResponse> writers = this.writers;

		byte unwriting = -1;

		for (; running;) {

			InnerResponse writer = writers.poll();
//			try {
//				writer = writers.poll(16,TimeUnit.MILLISECONDS);
//			} catch (InterruptedException e1) {
//				e1.printStackTrace();
//			}
			if (writer == null) {
				continue;
			}

			EndPoint endPoint = writer.getEndPoint();

			if (endPoint.isWriting(writer.getSessionID())) {
				continue;
			}

			try {

				writer.doWrite();

				if (writer.complete()) {

					endPoint.setWriting(unwriting);

				} else {

					writers.offer(writer);
				}
			} catch (IOException e) {

				e.printStackTrace();

				writer.catchException(writer.getInnerSession().getRequest(), writer, e);
			}
		}
	}

	public void doStart() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "EndPoint-Writer");
		this.owner.start();

	}

	public void doStop() throws Exception {
		running = false;
	}
}
