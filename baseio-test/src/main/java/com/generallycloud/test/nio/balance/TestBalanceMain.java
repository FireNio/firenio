/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.test.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.balance.BalanceServerBootStrap;
import com.generallycloud.nio.balance.FacadeInterceptorImpl;
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
		f.setFacadeInterceptor(new FacadeInterceptorImpl(500,50000));
		f.setBalanceRouter(new HashedBalanceRouter(10240));
//		f.setBalanceRouter(new SimpleNextRouter());
		
		f.startup();
	}
}
