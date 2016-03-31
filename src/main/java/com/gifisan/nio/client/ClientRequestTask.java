package com.gifisan.nio.client;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;

public class ClientRequestTask implements Runnable,LifeCycle {

	private BlockingQueue<ClientRequest>		requests		= new ArrayBlockingQueue<ClientRequest>(4);
	private Thread							owner		= null;
	private boolean						running		= false;
	private ClientConnection					connection	= null;

	public ClientRequestTask(ClientConnection connection) {
		this.connection = connection;
	}

	public void offer(ClientRequest request) {
		this.requests.offer(request);
	}

	public void run() {

		for (; running;) {

			ClientRequest request = null;
			try {
				request = requests.poll(16, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				DebugUtil.debug(e);
			}
			
			if (request == null) {
				continue;
			}
			
			try {
				connection.write(request.getSessionID(), request.getServiceName(), request.getContent());
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	public void start() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "Client-Requestor");
		this.owner.start();

	}

	public void stop() throws Exception {
		running = false;
	}

}
