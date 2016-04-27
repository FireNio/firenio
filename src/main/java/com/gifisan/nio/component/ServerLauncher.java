package com.gifisan.nio.component;

import java.lang.reflect.Method;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;


public class ServerLauncher {

	public void launch() throws Exception {

		try {
			
			SharedBundle bundle = SharedBundle.instance();
			
			bundle.loadLog4jProperties("conf/log4j.properties");

			bundle.storageProperties("conf/server.properties");

			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);

			Class clazz = Class.forName("com.gifisan.nio.server.NIOServer");

			Object instance = clazz.newInstance();

			Method start = instance.getClass().getMethod("start");

			start.invoke(instance);

//			new NIOServer().start();

		} catch (Throwable e) {
			LoggerFactory.getLogger(ServerLauncher.class).error(e.getMessage(), e);
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();

		launcher.launch();

	}
}
