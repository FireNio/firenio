package com.generallycloud.test.nio.common;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
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
			
			context.addSessionEventListener(new LoggerSEListener());

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(IoConnectorUtil.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}
	}
}
