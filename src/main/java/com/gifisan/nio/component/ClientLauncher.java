package com.gifisan.nio.component;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ServerProtocolDecoder;


public class ClientLauncher {
	
	private TCPConnector connector = new TCPConnector();

	public TCPConnector getTCPConnector() throws Exception {

		ApplicationContext applicationContext = new ApplicationContext();
		
		NIOContext context = new DefaultNIOContext();
		
		try {
			
			PropertiesLoader.load();
			
			SharedBundle bundle = SharedBundle.instance();
			
			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);
			
			applicationContext.setContext(context);
			
			applicationContext.start();
			
			context.setProtocolDecoder(new ServerProtocolDecoder());
			
			context.setIOEventHandle(new FixedIOEventHandle(applicationContext));
			
			context.addSessionEventListener(new DefaultSessionEventListener());
			
			connector.setContext(context);
			
			connector.connect();
			
			return connector;

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(ClientLauncher.class).error(e.getMessage(), e);
			
			LifeCycleUtil.stop(applicationContext);
			
			CloseUtil.close(connector);
			
			return connector;
		}
	}

	public static void main(String[] args) throws Exception {
		
		ClientLauncher launcher = new ClientLauncher();

		IOConnector connector = launcher.getTCPConnector();
		
		CloseUtil.close(connector);
		
	}
}
