package com.gifisan.mtp.component;

import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.server.MTPServer;

public class MTPServerLauncher {

	
	public void launch() throws Exception{
		SharedBundle bundle = SharedBundle.instance();
		
		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
		
		if (!debug) {
			bundle.loadLog4jProperties(MTPServerLauncher.class, "conf/log4j.properties");
			
			bundle.storageProperties(MTPServerLauncher.class, "conf/server.properties");
		}
		
		int serverPort = bundle.getIntegerProperty("SERVER.PORT");
		
		if (serverPort == 0) {
			throw new Exception("未设置服务端口或端口为0");
		}
		
		new MTPServer(serverPort).start();
		
	}
	
	
}
