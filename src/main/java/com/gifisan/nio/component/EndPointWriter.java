package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.concurrent.LinkedListM2O;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.WriteFuture;

public class EndPointWriter extends AbstractLifeCycle implements Runnable {

	private Thread							owner	= null;
	private boolean						running	= false;
	private LinkedListM2O<WriteFuture>			writers	= new LinkedListM2O<WriteFuture>();

	public void offer(WriteFuture writer) {
		this.writers.offer(writer);
	}

	public void run() {

		byte unwriting = -1;

		for (; running;) {

			WriteFuture writer = writers.poll(16);
			
			if (writer == null) {
				continue;
			}
			
			Session session = writer.getSession();
			
			EndPoint endPoint = writer.getEndPoint();
			
			if (endPoint.isNetworkWeak()) {
				
				endPoint.addWriter(writer);
				
				endPoint.interestWrite();
				
				continue;
			}
			
			byte sessionID = session.getSessionID();

			if (!endPoint.canWrite(sessionID)) {
				continue;
			}

			try {

				if (writer.write()) {

					endPoint.setWriting(unwriting);
					
				} else {
					
					if (writer.isNetworkWeak()) {
						
						endPoint.addWriter(writer);
						
						endPoint.interestWrite();
						
						continue;
					}
					
					endPoint.setWriting(sessionID);

					writers.offer(writer);
				}
			} catch (IOException e) {

				e.printStackTrace();

				writer.catchException(e);
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
