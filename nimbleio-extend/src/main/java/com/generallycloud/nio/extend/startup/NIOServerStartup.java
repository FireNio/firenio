package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.DatagramChannelAcceptor;
import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.ManagerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.configuration.ServerConfigurationLoader;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.FixedIOEventHandle;
import com.generallycloud.nio.extend.configuration.ApplicationConfiguration;
import com.generallycloud.nio.extend.configuration.ApplicationConfigurationLoader;
import com.generallycloud.nio.extend.configuration.FileSystemACLoader;

public class NIOServerStartup {

	private Logger		logger	= LoggerFactory.getLogger(NIOServerStartup.class);

	public void launch(String base) throws Exception {
		
		SharedBundle.instance().loadAllProperties(base);
		
		ApplicationConfigurationLoader acLoader = new FileSystemACLoader(base + "/conf/");

		ApplicationConfiguration ac = acLoader.loadConfiguration(SharedBundle.instance());
		
		ApplicationContext applicationContext = new ApplicationContext(ac,base);

		ServerConfigurationLoader configurationLoader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = configurationLoader.loadConfiguration(SharedBundle.instance());

		EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
				"IOEvent", 
				configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
				configuration.getSERVER_CORE_SIZE());

		NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		DatagramChannelAcceptor udpAcceptor = new DatagramChannelAcceptor();

		try {

			applicationContext.setContext(context);

			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSEListener());

			context.addSessionEventListener(new ManagerSEListener());

			context.setProtocolFactory(new NIOProtocolFactory());
			
			acceptor.setContext(context);

			acceptor.bind();

			udpAcceptor.setContext(context);

			udpAcceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();

			udpAcceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		
		NIOServerStartup launcher = new NIOServerStartup();

		launcher.launch("");
	}
}
