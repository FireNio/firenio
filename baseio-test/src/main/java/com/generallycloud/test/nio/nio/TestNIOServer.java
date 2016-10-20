package com.generallycloud.test.nio.nio;

import com.generallycloud.nio.extend.startup.NIOServerStartup;


public class TestNIOServer {

	public static void main(String[] args) throws Exception {
		
		NIOServerStartup s = new NIOServerStartup();
		
		s.launch("nio");
	}
}
