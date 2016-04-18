package com.gifisan.nio.component;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerLauncher {

	private Logger		logger	= LoggerFactory.getLogger(ServerLauncher.class);

	public void launch() throws Exception {

		try {

			logger.info("     [NIOServer] ======================================= 服务开始启动 =======================================");

			Class clazz = Class.forName("com.gifisan.nio.server.NIOServer");

			Object instance = clazz.newInstance();

			Method start = instance.getClass().getMethod("start");

			start.invoke(instance);

//			new NIOServer().start();

		} catch (Throwable e) {
			logger.error("启动失败：" + e.getMessage(), e);
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();

		launcher.launch();

	}
}
