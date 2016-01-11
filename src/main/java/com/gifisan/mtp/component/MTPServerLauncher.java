package com.gifisan.mtp.component;

import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.server.MTPServer;

public class MTPServerLauncher {

	
	public void launch() throws Exception{
		SharedBundle bundle = SharedBundle.instance();
		
		int serverPort = bundle.getIntegerProperty("SERVER.PORT");
		
		if (serverPort == 0) {
			throw new Exception("未设置服务端口或端口为0");
		}
		
		new MTPServer(serverPort).start();
		
	}
	
	
}
