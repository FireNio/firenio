package com.generallycloud.test.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.balance.BalanceServerBootStrap;
import com.generallycloud.nio.balance.router.HashedBalanceRouter;
import com.generallycloud.nio.codec.protobase.ProtobaseProtocolFactory;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class TestBalanceMain {

	public static void main(String[] args) throws IOException {

		BalanceServerBootStrap f = new BalanceServerBootStrap();
		
		f.setBalanceProtocolFactory(new ProtobaseProtocolFactory());
		f.setBalanceReverseProtocolFactory(new ProtobaseProtocolFactory());
		
		
		ServerConfiguration fc = new ServerConfiguration();
		fc.setSERVER_PORT(8600);
		
		ServerConfiguration frc = new ServerConfiguration();
		frc.setSERVER_PORT(8800);
		
		f.setBalanceServerConfiguration(fc);
		f.setBalanceReverseServerConfiguration(frc);
		f.setBalanceRouter(new HashedBalanceRouter(10240));
//		f.setBalanceRouter(new SimpleNextRouter());
		
		f.startup();
	}
}
