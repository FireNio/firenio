package com.generallycloud.test.reconnect;

import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.ReconnectableConnector;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestReconnectClient {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {

			}
		};
		
		SocketChannelContext context = new SocketChannelContextImpl(new ServerConfiguration("localhost", 18300));

		ReconnectableConnector connector = new ReconnectableConnector(context);
		
		connector.setRetryTime(5000);

		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());

		context.setProtocolFactory(new FixedLengthProtocolFactory());
		
		connector.connect();
		
	}
}
