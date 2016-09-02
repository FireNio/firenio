package com.generallycloud.nio.extend;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SessionAliveSEListener;
import com.generallycloud.nio.configuration.ServerConfiguration;

public class IOAcceptorUtil {

	private static Logger	logger	= LoggerFactory.getLogger(IOAcceptorUtil.class);

	public static TCPAcceptor getTCPAcceptor(IOEventHandleAdaptor ioEventHandleAdaptor) {

		return getTCPAcceptor(ioEventHandleAdaptor, null);
	}

	public static TCPAcceptor getTCPAcceptor(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) {

		TCPAcceptor acceptor = new TCPAcceptor();

		try {
			
			configuration.setSERVER_IS_ACCEPT_BEAT(true);

			NIOContext context = new DefaultNIOContext(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

			context.addSessionEventListener(new SessionAliveSEListener());
			
			acceptor.setContext(context);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			acceptor.unbind();

			throw new RuntimeException(e);
		}

		return acceptor;
	}

}
