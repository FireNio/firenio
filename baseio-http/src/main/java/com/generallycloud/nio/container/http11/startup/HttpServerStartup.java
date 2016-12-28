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
package com.generallycloud.nio.container.http11.startup;

import java.io.File;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.WebSocketBeatFutureFactory;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSessionAliveSEListener;
import com.generallycloud.nio.component.ssl.SSLUtil;
import com.generallycloud.nio.component.ssl.SslContext;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.configuration.ServerConfigurationLoader;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.ExtendIOEventHandle;
import com.generallycloud.nio.container.configuration.ApplicationConfiguration;
import com.generallycloud.nio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.nio.container.configuration.FileSystemACLoader;
import com.generallycloud.nio.container.http11.service.FutureAcceptorHttpFilter;

public class HttpServerStartup {
	
	private Logger logger = LoggerFactory.getLogger(HttpServerStartup.class);

	public void launch(String base) throws Exception {

		SharedBundle.instance().loadAllProperties(base);
		
		ApplicationConfigurationLoader acLoader = new FileSystemACLoader(base + "/conf/");

		ApplicationConfiguration ac = acLoader.loadConfiguration(SharedBundle.instance());
		
		ApplicationContext applicationContext = new ApplicationContext(ac,base);
		
		ServerConfigurationLoader configurationLoader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = configurationLoader.loadConfiguration(SharedBundle.instance());

		SocketChannelContext context = new SocketChannelContextImpl(configuration);
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {
			
			applicationContext.setContext(context);
			
			context.setBeatFutureFactory(new WebSocketBeatFutureFactory());

			context.setIoEventHandleAdaptor(new ExtendIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSocketSEListener());
			
			context.addSessionEventListener(new SocketSessionAliveSEListener());
			
			context.setProtocolFactory(new ServerHTTPProtocolFactory());
			
			if (configuration.isSERVER_ENABLE_SSL()) {
				
				File certificate = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.crt");
				File privateKey = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.key");
				
//				File certificate = SharedBundle.instance().loadFile(base + "/conf/keyutil_127.0.0.1.crt");
//				File privateKey = SharedBundle.instance().loadFile(base + "/conf/keyutil_127.0.0.1.key");
				
				SslContext sslContext = SSLUtil.initServer(privateKey,certificate);
				
				context.setSslContext(sslContext);
			}
			
			acceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();
		}
	}
	
	public static void main(String[] args) throws Exception {
		
		HttpServerStartup startup = new HttpServerStartup();
		
		startup.launch("http");
	}
}
