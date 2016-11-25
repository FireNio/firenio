package com.generallycloud.test.nio.common;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class IoAcceptorUtil {

	private static Logger	logger	= LoggerFactory.getLogger(IoAcceptorUtil.class);

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor IoEventHandleAdaptor) throws Exception {

		return getTCPAcceptor(IoEventHandleAdaptor, null);
	}

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor IoEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}
		
		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			context.setIoEventHandleAdaptor(IoEventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			acceptor.unbind();

			throw new RuntimeException(e);
		}

		return acceptor;
	}

}
