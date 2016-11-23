package com.generallycloud.test.nio.common;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class IoAcceptorUtil {

	private static Logger	logger	= LoggerFactory.getLogger(IoAcceptorUtil.class);

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor ioEventHandleAdaptor) throws Exception {

		return getTCPAcceptor(ioEventHandleAdaptor, null);
	}

	public static SocketChannelAcceptor getTCPAcceptor(IoEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) throws Exception {
		
		if (configuration == null) {
			PropertiesSCLoader loader = new PropertiesSCLoader();
			configuration = loader.loadConfiguration(SharedBundle.instance());
		}

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		try {
			
			BaseContext context = new BaseContextImpl(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

			acceptor.setContext(context);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			acceptor.unbind();

			throw new RuntimeException(e);
		}

		return acceptor;
	}

}
