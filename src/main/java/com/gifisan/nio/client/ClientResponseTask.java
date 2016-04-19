package com.gifisan.nio.client;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;

public class ClientResponseTask implements Runnable, LifeCycle {

	private Thread				owner			= null;
	private boolean			running			= false;
	private ClientConnection		connection		= null;
	private MessageBus[]		buses			= null;
	private ReentrantLock		lock				= new ReentrantLock();
	private Condition			inputsreamComplete	= lock.newCondition();

	public ClientResponseTask(ClientConnection connection, MessageBus[] buses) {
		this.connection = connection;
		this.buses = buses;
		lock.newCondition();
	}

	public void run() {

		for (; running;) {

			try {

				ClientResponse response = connection.acceptResponse();

				if (response == null) {

					break;
				}
				
				EndPointInputStream inputStream = response.getInputStream();

				if (inputStream != null) {
					
					waitStream(response, inputStream);
					
					continue;
				}

				MessageBus bus = buses[response.getSessionID()];

				bus.setResponse(response);
				
			} catch (IOException e) {
				e.printStackTrace();
				this.running = false;
			}
		}
	}
	
	private void waitStream(ClientResponse response,EndPointInputStream inputStream){
		
		ReentrantLock lock = this.lock;
		
		Condition inputsreamComplete = this.inputsreamComplete;
		
		inputStream.setLock(lock, inputsreamComplete);

		lock.lock();
		
		try {
			
			MessageBus bus = buses[response.getSessionID()];

			bus.setResponse(response);
			
			inputsreamComplete.await();
		} catch (InterruptedException e) {
			DebugUtil.debug(e);
			inputsreamComplete.signal();
		}
		
		lock.unlock();
	}

	public void start() {
		this.running = true;
		this.owner = new Thread(this, "Client-Response-acceptor");
		this.owner.start();

	}

	public void stop() throws Exception {
		running = false;
	}

}
