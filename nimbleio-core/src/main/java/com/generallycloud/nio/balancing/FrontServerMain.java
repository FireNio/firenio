package com.generallycloud.nio.balancing;

import java.io.IOException;

import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.protocol.ProtocolFactory;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;

public class FrontServerMain {

	public void startup(ProtocolFactory protocolFactory) throws IOException{
		
		PropertiesLoader.setBasepath("nio");
		
		FrontConfiguration configuration = new FrontConfiguration();
		configuration.setFRONT_FACADE_PORT(8600);
		configuration.setFRONT_REVERSE_PORT(8800);
		
		FrontFacadeAcceptor frontFacadeAcceptor = new FrontFacadeAcceptor();
		
		frontFacadeAcceptor.start(configuration,protocolFactory);
	}
	
	public static void main(String[] args) throws IOException {
		
		
		FrontServerMain main = new FrontServerMain();
		
		main.startup(new NIOProtocolFactory());
	}
}
