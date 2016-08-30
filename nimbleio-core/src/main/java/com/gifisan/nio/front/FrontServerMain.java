package com.gifisan.nio.front;

import java.io.IOException;

import com.gifisan.nio.common.PropertiesLoader;

public class FrontServerMain {

	public void startup() throws IOException{
		
		PropertiesLoader.setBasepath("nio");
		
		FrontConfiguration configuration = new FrontConfiguration();
		configuration.setFRONT_FACADE_PORT(8600);
		configuration.setFRONT_REVERSE_PORT(8800);
		
		FrontFacadeAcceptor frontFacadeAcceptor = new FrontFacadeAcceptor();
		
		frontFacadeAcceptor.start(configuration);
	}
	
	public static void main(String[] args) throws IOException {
		
		
		FrontServerMain main = new FrontServerMain();
		
		main.startup();
	}
}
