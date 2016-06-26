package com.gifisan.nio.extend;

import com.gifisan.nio.acceptor.ServerProtocolDecoder;
import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.DefaultSessionEventListener;
import com.gifisan.nio.component.NIOContext;


public class ServerLauncher {

	public void launch() throws Exception {
		
		PropertiesLoader.load();
		
		SharedBundle bundle = SharedBundle.instance();

		ApplicationContext applicationContext = new ApplicationContext();
		
		NIOContext context = new DefaultNIOContext();
		
		TCPAcceptor acceptor = new TCPAcceptor();
		
		try {
			
			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);
			
			applicationContext.setContext(context);
			
			context.setProtocolDecoder(new ServerProtocolDecoder());
			
			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));
			
			context.addSessionEventListener(new DefaultSessionEventListener());
			
			acceptor.setContext(context);
			
			acceptor.bind();

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(ServerLauncher.class).error(e.getMessage(), e);
			
			LifeCycleUtil.stop(applicationContext);
			
			acceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		ServerLauncher launcher = new ServerLauncher();

		launcher.launch();

	}
}
