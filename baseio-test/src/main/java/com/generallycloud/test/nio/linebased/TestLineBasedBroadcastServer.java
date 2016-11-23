package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedBroadcastServer {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
			
			public void accept(Session session, ReadFuture future) throws Exception {
				
				LineBasedReadFuture f = (LineBasedReadFuture) future;
				
				long old = System.currentTimeMillis();
				
				String res = "hello world!";
				
				f.write(res);
				
				ChannelAcceptor acceptor = (ChannelAcceptor) session.getContext().getChannelService();
				
				acceptor.broadcast(future);
				
				long now = System.currentTimeMillis();
				
				System.out.println("广播花费时间："+(now - old)+",连接数："+session.getContext().getSessionManager().getManagedSessionSize());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_PORT(18300);
		
		configuration.setSERVER_SESSION_IDLE_TIME(180000);
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY(1024 * 512);
		
		configuration.setSERVER_MEMORY_POOL_UNIT(64);
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		BaseContext context = new BaseContextImpl(configuration);
		
		context.addSessionEventListener(new LoggerSEListener());
		
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new LineBasedProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
