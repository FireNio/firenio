package test;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class ServerUtil {

	private static TCPAcceptor	acceptor;

	public static TCPAcceptor getTCPAcceptor(IOEventHandleAdaptor ioEventHandleAdaptor) {

		return getTCPAcceptor(ioEventHandleAdaptor, null);
	}

	public static TCPAcceptor getTCPAcceptor(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) {

		if (acceptor == null) {

			try {

				acceptor = new TCPAcceptor();

				NIOContext context = new DefaultNIOContext();

				context.setServerConfiguration(configuration);

				context.setIOEventHandleAdaptor(ioEventHandleAdaptor);

				context.addSessionEventListener(new LoggerSEtListener());

				acceptor.setContext(context);

			} catch (Throwable e) {

				LoggerFactory.getLogger(ClientUtil.class).error(e.getMessage(), e);

				acceptor.unbind();

				throw new RuntimeException(e);
			}
		}

		return acceptor;
	}

}
