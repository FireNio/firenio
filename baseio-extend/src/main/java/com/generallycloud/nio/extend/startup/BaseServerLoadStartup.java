package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class BaseServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				BaseReadFuture f = (BaseReadFuture)future;
				f.write("yes server already accept your message");
				f.write(f.getReadText());
				session.flush(future);
			}
		};

		PropertiesSCLoader loader = new PropertiesSCLoader();
		ServerConfiguration configuration = loader.loadConfiguration(SharedBundle.instance());

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		try {

			BaseContext context = new BaseContextImpl(configuration);

			context.setIoEventHandleAdaptor(eventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

			acceptor.setContext(context);
			
			acceptor.getContext().setProtocolFactory(new BaseProtocolFactory());

			acceptor.bind();

		} catch (Throwable e) {

			acceptor.unbind();

			throw new RuntimeException(e);
		}
	}
	
}
