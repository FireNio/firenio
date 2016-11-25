package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class BaseServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(SocketSession session, ReadFuture future) throws Exception {
				BaseReadFuture f = (BaseReadFuture)future;
				f.write("yes server already accept your message");
				f.write(f.getReadText());
				session.flush(future);
			}
		};

		PropertiesSCLoader loader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = loader.loadConfiguration(SharedBundle.instance());

		SocketChannelContext context = new SocketChannelContextImpl(configuration);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		try {

			context.setIoEventHandleAdaptor(eventHandleAdaptor);

			context.addSessionEventListener(new LoggerSocketSEListener());

			acceptor.getContext().setProtocolFactory(new BaseProtocolFactory());

			acceptor.bind();

		} catch (Throwable e) {

			acceptor.unbind();

			throw new RuntimeException(e);
		}
	}
	
}
