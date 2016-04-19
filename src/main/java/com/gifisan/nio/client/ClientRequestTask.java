package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.concurrent.LinkedListM2O;

public class ClientRequestTask implements Runnable,LifeCycle {

	private LinkedListM2O<ClientRequest>		requests		= new LinkedListM2O<ClientRequest>(5000);
	private Thread							owner		= null;
	private boolean						running		= false;
	private ClientConnection					connection	= null;
	private ClientConnector					connector		= null;

	public ClientRequestTask(ClientConnector connector) {
		this.connector = connector;
		this.connection = connector.getClientConnection();
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
				
				if (request.getInputStream() != null) {
					connection.write(
							request.getSessionID(), 
							request.getServiceName(), 
							request.getText(),
							request.getInputStream());
					continue;
				}
				
				if (request.getServiceName() == null) {
					connection.writeBeat();
					continue;
				}
				
				connection.write(
						request.getSessionID(), 
						request.getServiceName(), 
						request.getText());
			} catch (IOException e) {
				e.printStackTrace();
				running = false;
				CloseUtil.close(connector);
			}
		}
	}

	public void start() {
		this.running = true;
		this.owner = new Thread(this, "Client-Requestor");
		this.owner.start();

	}

	public void stop() throws Exception {
		running = false;
	}

}
