package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.service.ResponseWriter;

public class EndPointWriter extends AbstractLifeCycle implements Runnable {

	private Thread							owner	= null;
	private boolean						running	= false;
	private LinkedListM2O<ResponseWriter>		writers	= new LinkedListM2O<ResponseWriter>();
//	private BlockingQueue<InnerResponse> writers = new ArrayBlockingQueue<InnerResponse>(10000 * 100);

	public void offer(ResponseWriter writer) {
		this.writers.offer(writer);
	}

	public void run() {

//		BlockingQueue<InnerResponse> writers = this.writers;
//		LinkedListM2O<InnerResponse> writers = this.writers;

		byte unwriting = -1;

		for (; running;) {

			ResponseWriter writer = writers.poll(16);
			
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

				writer.catchException(writer.getRequest(), e);
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
