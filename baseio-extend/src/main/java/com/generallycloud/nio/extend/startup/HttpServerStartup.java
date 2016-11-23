package com.generallycloud.nio.extend.startup;

import java.io.File;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.WebSocketBeatFutureFactory;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ssl.SSLUtil;
import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.SessionAliveSEListener;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.configuration.ServerConfigurationLoader;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ExtendIOEventHandle;
import com.generallycloud.nio.extend.configuration.ApplicationConfiguration;
import com.generallycloud.nio.extend.configuration.ApplicationConfigurationLoader;
import com.generallycloud.nio.extend.configuration.FileSystemACLoader;
import com.generallycloud.nio.extend.service.FutureAcceptorHttpFilter;

public class HttpServerStartup {
	
	private Logger logger = LoggerFactory.getLogger(HttpServerStartup.class);

	public void launch(String base) throws Exception {

		SharedBundle.instance().loadAllProperties(base);
		
		ApplicationConfigurationLoader acLoader = new FileSystemACLoader(base + "/conf/");

		ApplicationConfiguration ac = acLoader.loadConfiguration(SharedBundle.instance());
		
		ApplicationContext applicationContext = new ApplicationContext(ac,base);
		
		ServerConfigurationLoader configurationLoader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = configurationLoader.loadConfiguration(SharedBundle.instance());

		BaseContext context = new BaseContextImpl(configuration);
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		try {
			
			applicationContext
					.setLastServiceFilter(new FutureAcceptorHttpFilter(applicationContext.getClassLoader()));
			
			applicationContext.setContext(context);
			
			context.setBeatFutureFactory(new WebSocketBeatFutureFactory());

			context.setIoEventHandleAdaptor(new ExtendIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSEListener());
			
			context.addSessionEventListener(new SessionAliveSEListener());
			
//			context.addSessionEventListener(new SessionActiveSEListener());
			
			context.setProtocolFactory(new ServerHTTPProtocolFactory());
			
			if (configuration.isSERVER_ENABLE_SSL()) {
				
				File certificate = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.crt");
				File privateKey = SharedBundle.instance().loadFile(base + "/conf/generallycloud.com.key");
				
//				File certificate = SharedBundle.instance().loadFile(base + "/conf/keyutil_127.0.0.1.crt");
//				File privateKey = SharedBundle.instance().loadFile(base + "/conf/keyutil_127.0.0.1.key");
				
				SslContext sslContext = SSLUtil.initServer(privateKey,certificate);
				
				context.setSslContext(sslContext);
			}
			
			acceptor.setContext(context);

			acceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();
		}
	}
	
}
