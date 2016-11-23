package com.generallycloud.test.nio.common;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class IoConnectorUtil {

	public static SocketChannelConnector getTCPConnector(IoEventHandleAdaptor ioEventHandleAdaptor) throws Exception {
		return getTCPConnector(ioEventHandleAdaptor, null);
	}

	public static SocketChannelConnector getTCPConnector(IoEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.5);
		
		SocketChannelConnector connector = null;

		try {

			connector = new SocketChannelConnector();

			BaseContext context = new BaseContextImpl(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);
			
			context.addSessionEventListener(new LoggerSEListener());

			connector.setContext(context);

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(IoConnectorUtil.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}
	}
}
