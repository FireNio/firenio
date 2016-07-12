package com.gifisan.nio.extend;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.acceptor.UDPAcceptor;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.protocol.http11.HTTPProtocolFactory;


public class ServerLauncher {

	public void launch() throws Exception {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		NIOContext context = new DefaultNIOContext();
		
		TCPAcceptor acceptor = new TCPAcceptor();
		
		UDPAcceptor udpAcceptor = new UDPAcceptor();
		
		try {
			
			applicationContext.setContext(context);
			
			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));
			
			context.addSessionEventListener(new LoggerSEtListener());
			
			context.setProtocolFactory(new HTTPProtocolFactory());
			
			acceptor.setContext(context);
			
			acceptor.bind();
			
			udpAcceptor.setContext(context);
			
			udpAcceptor.bind();

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(ServerLauncher.class).error(e.getMessage(), e);
			
			LifeCycleUtil.stop(applicationContext);
			
			acceptor.unbind();
			
			udpAcceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();

		launcher.launch();

	}
}
