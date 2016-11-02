package com.generallycloud.test.nio.http2;

import com.generallycloud.nio.extend.startup.Http2ServerStartup;

public class TestHttp2SserverStartup {

	public static void main(String[] args) throws Exception {
		
		Http2ServerStartup serverStartup = new Http2ServerStartup();
		
		serverStartup.launch("http");
	}
	
}
