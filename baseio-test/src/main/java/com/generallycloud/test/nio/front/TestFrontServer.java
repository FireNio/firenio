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
