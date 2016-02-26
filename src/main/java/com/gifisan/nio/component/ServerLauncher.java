package com.gifisan.nio.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.server.NIOServer;

public class ServerLauncher {

	private Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
	
	public void launch() throws Exception{
		SharedBundle bundle = SharedBundle.instance();
		
		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
		
		if (!debug) {
			bundle.loadLog4jProperties(ServerLauncher.class, "conf/log4j.properties");
			
			bundle.storageProperties(ServerLauncher.class, "conf/server.properties");
		}
		
		int serverPort = bundle.getIntegerProperty("SERVER.PORT");
		
		if (serverPort == 0) {
			throw new Exception("未设置服务端口或端口为0");
		}
		
		try {
			new NIOServer(serverPort).start();
		} catch (Throwable e) {
			logger.error("启动失败："+e.getMessage(),e);
		}
	}
	
	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();
		
		launcher.launch();
		
	}
}
