package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
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
				
				ChannelAcceptor acceptor = (ChannelAcceptor) session.getContext().getSocketChannelService();
				
				acceptor.broadcast(future);
				
				long now = System.currentTimeMillis();
				
				System.out.println("广播花费时间："+(now - old)+",连接数："+session.getContext().getSessionFactory().getManagedSessionSize());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_TCP_PORT(18300);
		
		configuration.setSERVER_SESSION_IDLE_TIME(180000);
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY(1024 * 512);
		
		configuration.setSERVER_MEMORY_POOL_UNIT(64);
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		BaseContext context = new BaseContextImpl(configuration);
		
		context.addSessionEventListener(new LoggerSEListener());
		
		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new LineBasedProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
