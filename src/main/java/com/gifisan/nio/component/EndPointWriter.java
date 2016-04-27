package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.concurrent.LinkedList;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class EndPointWriter extends AbstractLifeCycle implements Runnable {

	private Thread							owner	= null;
	private boolean						running	= false;
	private LinkedList<IOWriteFuture>			writers	= new LinkedListM2O<IOWriteFuture>();

	public void offer(IOWriteFuture writer) {
		
		this.writers.forceOffer(writer);
	}

	public void run() {

		byte unwriting = 0;

		for (; running;) {

			IOWriteFuture writer = writers.poll(16);
			
			if (writer == null) {
				continue;
			}
			
			EndPoint endPoint = writer.getEndPoint();
			
			if (endPoint.isNetworkWeak()) {
				
				endPoint.addWriter(writer);
				
				endPoint.interestWrite();
				
				continue;
			}
			
			if (!endPoint.enableWriting(writer.getFutureID())) {
				writers.offer(writer);
				continue;
			}

			try {

				if (writer.write()) {
					
					endPoint.setWriting(unwriting);
					
				} else {
					
					if (writer.isNetworkWeak()) {
						
						endPoint.setWriting(writer.getFutureID());
						
						endPoint.setCurrentWriter(writer);
						
//						endPoint.addWriter(writer);
						
						endPoint.interestWrite();
						
						continue;
					}
					
					endPoint.setWriting(writer.getFutureID());
					
					writers.forceOffer(writer);
				}
			} catch (Exception e) {

				e.printStackTrace();
				
				//FIXME

				writer.catchException(new IOException(e));
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
