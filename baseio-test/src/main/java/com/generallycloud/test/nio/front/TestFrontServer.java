package com.generallycloud.test.nio.front;

import java.io.IOException;

import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.front.FrontServerBootStrap;

public class TestFrontServer {

	
	public static void main(String[] args) throws IOException {

		FrontServerBootStrap f = new FrontServerBootStrap();
		
		f.setFrontProtocolFactory(new ProtobaseProtocolFactory());
		f.setFrontReverseProtocolFactory(new ProtobaseProtocolFactory());
		
		
		ServerConfiguration fc = new ServerConfiguration();
		fc.setSERVER_PORT(8900);
		
		ServerConfiguration frc = new ServerConfiguration();
		frc.setSERVER_PORT(8600);
		
		f.setFrontServerConfiguration(fc);
		f.setFrontReverseServerConfiguration(frc);
		
		f.startup();
	}
	
}
