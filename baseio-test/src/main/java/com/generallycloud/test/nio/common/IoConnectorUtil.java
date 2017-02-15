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
package com.generallycloud.test.nio.common;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class IoConnectorUtil {

	public static SocketChannelConnector getTCPConnector(IoEventHandleAdaptor IoEventHandleAdaptor) throws Exception {
		return getTCPConnector(IoEventHandleAdaptor, null);
	}

	public static SocketChannelConnector getTCPConnector(IoEventHandleAdaptor IoEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.5);

		SocketChannelContext context = new SocketChannelContextImpl(configuration);
		
		SocketChannelConnector connector = new SocketChannelConnector(context);

		try {

			context.setIoEventHandleAdaptor(IoEventHandleAdaptor);
			
			context.addSessionEventListener(new LoggerSocketSEListener());

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(IoConnectorUtil.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}
	}
}
