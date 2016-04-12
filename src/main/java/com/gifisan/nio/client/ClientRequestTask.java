package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ClientRequestTask implements Runnable,LifeCycle {

	private LinkedListM2O<ClientRequest>		requests		= new LinkedListM2O<ClientRequest>(4);
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

			ClientRequest request = requests.poll(16);
			
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
