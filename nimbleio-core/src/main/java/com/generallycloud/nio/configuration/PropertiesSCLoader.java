package com.generallycloud.nio.configuration;

import java.nio.charset.Charset;

import com.generallycloud.nio.common.SharedBundle;

public class PropertiesSCLoader implements ServerConfigurationLoader{

	public ServerConfiguration loadConfiguration(SharedBundle bundle) throws Exception {
		
		ServerConfiguration configuration = new ServerConfiguration();

		String encoding = bundle.getProperty("SERVER.ENCODING", "GBK");

		configuration.setSERVER_CORE_SIZE(Runtime.getRuntime().availableProcessors());
		configuration.setSERVER_DEBUG(bundle.getBooleanProperty("SERVER.DEBUG"));
		configuration.setSERVER_HOST(bundle.getProperty("SERVER.HOST"));
		configuration.setSERVER_TCP_PORT(bundle.getIntegerProperty("SERVER.TCP_PORT"));
		configuration.setSERVER_UDP_PORT(bundle.getIntegerProperty("SERVER.UDP_PORT"));
		configuration.setSERVER_ENCODING(Charset.forName(encoding));
		configuration.setSERVER_CHANNEL_QUEUE_SIZE(
				bundle.getIntegerProperty("SERVER.CHANNEL_QUEUE_SIZE",
				configuration.getSERVER_CHANNEL_QUEUE_SIZE()));
		configuration.setSERVER_IS_ACCEPT_BEAT(bundle.getBooleanProperty("SERVER.IS_ACCEPT_BEAT"));
		configuration.setSERVER_SESSION_IDLE_TIME(
				bundle.getLongProperty("SERVER.SESSION_IDLE_TIME", 
				configuration.getSERVER_SESSION_IDLE_TIME()));

		return configuration;
	}
	
}
