package com.generallycloud.nio.extend;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.TCPConnector;

public class IOConnectorUtil {

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor) {
		return getTCPConnector(ioEventHandleAdaptor, null);
	}

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) {
		
		TCPConnector connector = null;

		try {

			connector = new TCPConnector();

			NIOContext context = new DefaultNIOContext(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);

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
