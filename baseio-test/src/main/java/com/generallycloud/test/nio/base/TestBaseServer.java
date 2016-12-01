package com.generallycloud.test.nio.base;

import com.generallycloud.nio.container.protobase.startup.BaseServerStartup;

public class TestBaseServer {

	public static void main(String[] args) throws Exception {
		
		BaseServerStartup s = new BaseServerStartup();
		
		s.launch("nio");
	}
}
