package com.generallycloud.test.nio.linebased;

import com.generallycloud.nio.acceptor.ChannelAcceptor;
import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.line.LineBasedProtocolFactory;
import com.generallycloud.nio.codec.line.future.LineBasedReadFuture;
import com.generallycloud.nio.component.SocketChannelContextImpl;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLineBasedBroadcastServer {

	public static void main(String[] args) throws Exception {

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {
			
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
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
		
		SocketChannelContext context = new SocketChannelContextImpl(configuration);
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);
		
		context.addSessionEventListener(new LoggerSocketSEListener());
		
		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.setProtocolFactory(new LineBasedProtocolFactory());

		acceptor.bind();
	}
}
