package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.configuration.ServerConfigurationLoader;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.ExtendIOEventHandle;
import com.generallycloud.nio.extend.configuration.ApplicationConfiguration;
import com.generallycloud.nio.extend.configuration.ApplicationConfigurationLoader;
import com.generallycloud.nio.extend.configuration.FileSystemACLoader;

public class BaseServerStartup {

	private Logger		logger	= LoggerFactory.getLogger(BaseServerStartup.class);

	public void launch(String base) throws Exception {
		
		SharedBundle.instance().loadAllProperties(base);
		
		ApplicationConfigurationLoader acLoader = new FileSystemACLoader(base + "/conf/");

		ApplicationConfiguration ac = acLoader.loadConfiguration(SharedBundle.instance());
		
		ApplicationContext applicationContext = new ApplicationContext(ac,base);

		ServerConfigurationLoader configurationLoader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = configurationLoader.loadConfiguration(SharedBundle.instance());

		BaseContext context = new BaseContextImpl(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			applicationContext.setContext(context);

			context.setIoEventHandleAdaptor(new ExtendIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSEListener());

			context.setProtocolFactory(new BaseProtocolFactory());
			
			acceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();
		}
	}

}
