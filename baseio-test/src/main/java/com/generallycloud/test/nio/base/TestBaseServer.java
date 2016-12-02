package com.generallycloud.test.nio.base;

import com.generallycloud.nio.container.protobase.startup.ProtobaseServerStartup;

public class TestBaseServer {

	public static void main(String[] args) throws Exception {
		
		ProtobaseServerStartup s = new ProtobaseServerStartup();
		
		s.launch("nio");
	}
}
