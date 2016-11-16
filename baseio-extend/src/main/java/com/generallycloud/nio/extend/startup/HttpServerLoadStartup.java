package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("http");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
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

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		try {

			BaseContext context = new BaseContextImpl(configuration);

			context.setIOEventHandleAdaptor(eventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

			acceptor.setContext(context);
			
			acceptor.getContext().setProtocolFactory(new ServerHTTPProtocolFactory());

			acceptor.bind();

		} catch (Throwable e) {

			acceptor.unbind();

			throw new RuntimeException(e);
		}
	}
}
