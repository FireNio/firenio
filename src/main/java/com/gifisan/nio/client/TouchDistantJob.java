package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;

public class TouchDistantJob implements Runnable {

	private ClientConnection connection = null;
	
	public TouchDistantJob(ClientConnection connection) {
		this.connection = connection;
	}

	public void run() {
		ClientConnection connection = this.connection;
		try {
			connection.writeBeat();
		} catch (IOException e) {
			CloseUtil.close(connection);
			e.printStackTrace();
			try {
				connection.connect();
			} catch (IOException e1) {
				e1.printStackTrace();
				CloseUtil.close(connection);
				connection.setNetworkWeak();
				connection.wakeup();
			}
		}
	}
}
