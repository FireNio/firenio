package com.gifisan.nio.component;

import java.lang.reflect.Method;

import com.gifisan.nio.common.LoggerFactory;


public class ServerLauncher {

	public void launch() throws Exception {

		try {

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
