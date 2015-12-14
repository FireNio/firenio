package com.gifisan.mtp.component;

import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.server.MTPServer;

public class MTPServerLauncher {

	
	public void launch() throws Exception{
		int serverPort = SharedBundle.getIntegerProperty("APP_SERVER_PORT");
		
		if (serverPort == 0) {
			throw new Exception("未设置服务端口或端口为0");
		}
		
		MTPServer server = new MTPServer();
		server.setPort(serverPort);
		server.start();
		
	}
	
	
}
