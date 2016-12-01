package com.generallycloud.nio.container.http11.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(SocketSession session, ReadFuture future) throws Exception {
				HttpReadFuture f = (HttpReadFuture) future;

				String res;

				if (f.hasBodyContent()) {

					byte[] array = f.getBodyContent();

					res = "yes server already accept your message :) </BR><PRE style='font-size: 18px;color: #FF9800;'>"
							+ new String(array) + "</PRE>";
				} else {
					res = "yes server already accept your message :) " + f.getRequestParams();
				}

				f.write(res);
				session.flush(f);
			}
		};

		PropertiesSCLoader loader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = loader.loadConfiguration(SharedBundle.instance());

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			context.setIoEventHandleAdaptor(eventHandleAdaptor);

			context.addSessionEventListener(new LoggerSocketSEListener());

			acceptor.getContext().setProtocolFactory(new ServerHTTPProtocolFactory());

			acceptor.bind();

		} catch (Throwable e) {

			acceptor.unbind();

			throw new RuntimeException(e);
		}
	}
}
