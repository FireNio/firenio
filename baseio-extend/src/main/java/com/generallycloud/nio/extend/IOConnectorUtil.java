package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;

public class IOConnectorUtil {

	public static SocketChannelConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor) throws Exception {
		return getTCPConnector(ioEventHandleAdaptor, null);
	}

	public static SocketChannelConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		SocketChannelConnector connector = null;

		try {

			connector = new SocketChannelConnector();

			BaseContext context = new BaseContextImpl(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);
			
			if (ioEventHandleAdaptor instanceof SimpleIOEventHandle) {
				
				SimpleIOEventHandle eventHandle = (SimpleIOEventHandle)ioEventHandleAdaptor;
				
				context.addSessionEventListener(new UpdateFixedSessionSEListener(eventHandle.getFixedSession()));
			}

			context.addSessionEventListener(new LoggerSEListener());

			context.addSessionEventListener(new ConnectorCloseSEListener(connector));

			connector.setContext(context);

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(IOConnectorUtil.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}
	}
}
