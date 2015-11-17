package test;

import com.yoocent.mtp.server.MTPServer;

public class TestServer {

	
	
	public static void main(String[] args) throws Exception {
		MTPServer server = new MTPServer();
		server.setPort(8080);
		
		server.start();
		
	}
}
