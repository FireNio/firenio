package com.gifisan.nio.component;

import java.lang.reflect.Method;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;


public class ServerLauncher {

	public void launch() throws Exception {

		Object instance = null;
		
		try {
			
			PropertiesLoader.load();
			
			SharedBundle bundle = SharedBundle.instance();
			
//			if (bundle.storageProperties("../classes/server.properties")) {
//				
//				bundle.loadLog4jProperties("../classes/log4j.properties");
//			}else{
//				
//				if(!bundle.storageProperties("conf/server.properties")){
//					throw new Error("conf/server.properties unexist");
//				}
//				
//				bundle.loadLog4jProperties("conf/log4j.properties");
//			}

			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);

			Class clazz = Class.forName("com.gifisan.nio.server.NIOServer");

			instance = clazz.newInstance();

			Method start = instance.getClass().getMethod("start");

			start.invoke(instance);

//			new NIOServer().start();

		} catch (Throwable e) {
			LoggerFactory.getLogger(ServerLauncher.class).error(e.getMessage(), e);
			
			LifeCycleUtil.stop((LifeCycle)instance);
			
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();

		launcher.launch();

	}
}
