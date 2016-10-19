package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.acceptor.IOAcceptor;
import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedBroadcastServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {
			
			public void accept(Session session, ReadFuture future) throws Exception {
				
				long old = System.currentTimeMillis();
				
				String res = "hello world!";
				
				future.write(res);
				
				IOAcceptor acceptor = (IOAcceptor) session.getContext().getTCPService();
				
				acceptor.broadcast(future);
				
				long now = System.currentTimeMillis();
				
				System.out.println("广播花费时间："+(now - old)+",连接数："+session.getContext().getSessionFactory().getManagedSessionSize());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_TCP_PORT(18300);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		NIOContext context = new DefaultNIOContext(configuration);
		
		context.addSessionEventListener(new LoggerSEListener());
		
		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new LineBasedProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
