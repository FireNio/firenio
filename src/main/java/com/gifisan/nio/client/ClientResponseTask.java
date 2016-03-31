package com.gifisan.nio.client;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.gifisan.nio.LifeCycle;

public class ClientResponseTask implements Runnable ,LifeCycle {

	private BlockingQueue<ClientRequest>		requests		= new ArrayBlockingQueue<ClientRequest>(4);
	private Thread							owner		= null;
	private boolean						running		= false;
	private ClientConnection					connection	= null;
	private MessageBus[]					buses		= null;

	
	public ClientResponseTask(ClientConnection connection, MessageBus[] buses) {
		this.connection = connection;
		this.buses = buses;
	}

	public void offer(ClientRequest request) {
		this.requests.offer(request);
	}

	public void run() {

		for (; running;) {
			
			try {
				
				ClientResponse response = connection.acceptResponse();
				
				if (response == null) {
					
					break;
				}
				
				MessageBus bus = buses[response.getSessionID()];
				
				bus.setResponse(response);
			} catch (IOException e) {
				e.printStackTrace();
				this.running = false;
			}
		}
	}

	public void start() throws Exception {
		this.running = true;
		this.owner = new Thread(this, "Client-Response-acceptor");
		this.owner.start();

	}

	public void stop() throws Exception {
		running = false;
	}

}
