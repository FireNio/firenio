package com.generallycloud.test.nio.front;

import java.io.IOException;

import com.generallycloud.nio.balance.FrontServerBootStrap;
import com.generallycloud.nio.balance.router.HashedFrontRouter;
import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class TestFrontMain {

	public static void main(String[] args) throws IOException {

		FrontServerBootStrap f = new FrontServerBootStrap();
		
		f.setFrontProtocolFactory(new NIOProtocolFactory());
		f.setFrontReverseProtocolFactory(new NIOProtocolFactory());
		
		
		ServerConfiguration fc = new ServerConfiguration();
		fc.setSERVER_TCP_PORT(8600);
		
		ServerConfiguration frc = new ServerConfiguration();
		frc.setSERVER_TCP_PORT(8800);
		
		f.setFrontServerConfiguration(fc);
		f.setFrontReverseServerConfiguration(frc);
		f.setFrontRouter(new HashedFrontRouter(1024));
		
		f.startup();
	}
}
