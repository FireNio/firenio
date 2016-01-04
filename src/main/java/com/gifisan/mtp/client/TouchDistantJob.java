package com.gifisan.mtp.client;

import java.io.IOException;

import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.schedule.Job;

public class TouchDistantJob implements Job {

	private ClientConnection connection = null;
	
	public TouchDistantJob(ClientConnection connection) {
		this.connection = connection;
	}

	public void schedule() {
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
