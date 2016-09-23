package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.extend.startup.HttpServerStartup;


public class TestHTTPServer {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("http");
		
		HttpServerStartup.main(args);
	}
}
