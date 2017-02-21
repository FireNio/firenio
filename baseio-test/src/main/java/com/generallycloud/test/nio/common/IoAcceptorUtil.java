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

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class IoAcceptorUtil {

	private static Logger	logger	= LoggerFactory.getLogger(IoAcceptorUtil.class);

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor IoEventHandleAdaptor) throws Exception {

		return getTCPAcceptor(IoEventHandleAdaptor, null);
	}

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor IoEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		NioSocketChannelContext context = new NioSocketChannelContext(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			context.setIoEventHandleAdaptor(IoEventHandleAdaptor);

			context.addSessionEventListener(new LoggerSocketSEListener());

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			acceptor.unbind();

			throw new RuntimeException(e);
		}

		return acceptor;
	}

}
