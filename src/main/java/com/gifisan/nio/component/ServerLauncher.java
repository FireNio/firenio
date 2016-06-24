package com.gifisan.nio.component;

import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ServerProtocolDecoder;
import com.gifisan.nio.server.TCPAcceptor;


public class ServerLauncher {

	public void launch() throws Exception {

		final ApplicationContext applicationContext = new ApplicationContext();
		
		NIOContext context = new DefaultNIOContext();
		
		TCPAcceptor acceptor = new TCPAcceptor();
		
		try {
			
			PropertiesLoader.load();
			
			SharedBundle bundle = SharedBundle.instance();
			
			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);
			
			applicationContext.setContext(context);
			
			context.setProtocolDecoder(new ServerProtocolDecoder());
			
			context.setIOEventHandle(new FixedIOEventHandle(applicationContext));
			
			context.addSessionEventListener(new DefaultSessionEventListener());
			
			context.addLifeCycleListener(new AbstractLifeCycleListener(){

				public void lifeCycleStarting(LifeCycle lifeCycle) {
					try {
						applicationContext.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				public void lifeCycleStopping(LifeCycle lifeCycle) {
					LifeCycleUtil.stop(applicationContext);
				}
			});
			
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
