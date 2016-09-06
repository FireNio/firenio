package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.concurrent.EventLoopGroup;
import com.generallycloud.nio.component.concurrent.SingleEventLoopGroup;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.TCPConnector;

public class IOConnectorUtil {

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor) throws Exception {
		return getTCPConnector(ioEventHandleAdaptor, null);
	}

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		TCPConnector connector = null;

		try {

			connector = new TCPConnector();

			configuration.setSERVER_IS_ACCEPT_BEAT(true);

			EventLoopGroup eventLoopGroup = new SingleEventLoopGroup(
					"IOEvent", 
					configuration.getSERVER_CHANNEL_QUEUE_SIZE(),
					1);

			NIOContext context = new DefaultNIOContext(configuration,eventLoopGroup);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);
			
			if (ioEventHandleAdaptor instanceof SimpleIOEventHandle) {
				
				SimpleIOEventHandle eventHandle = (SimpleIOEventHandle)ioEventHandleAdaptor;
				
				context.addSessionEventListener(new UpdateFixedSessionSEListener(eventHandle.getFixedSession()));
			}

			context.setProtocolFactory(new NIOProtocolFactory());
			
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
