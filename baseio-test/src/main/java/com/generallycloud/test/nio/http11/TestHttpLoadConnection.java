/*
 * Copyright 2015 GenerallyCloud.com
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
package com.generallycloud.test.nio.http11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestHttpLoadConnection {

	public static void main(String[] args) throws IOException {
		
		SharedBundle.instance().loadAllProperties("http");
		
		List<SocketChannelConnector> connectors = new ArrayList<SocketChannelConnector>();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("www.generallycloud.com");
		configuration.setSERVER_PORT(80);
		
		try {
			for (int i = 0; i < 999; i++) {
				
				if (i % 100 == 0) {
					System.out.println("i__________________"+i);
				}
				
				HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();
				
				SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor,configuration);
				
				connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
				
				connector.connect();
				
				connectors.add(connector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			for (SocketChannelConnector connector : connectors) {
				
				CloseUtil.close(connector);
			}
		}
	}
}
