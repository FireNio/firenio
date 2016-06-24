package com.gifisan.nio.component;

import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.client.FixedIOSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ServerProtocolDecoder;
import com.gifisan.nio.server.service.FutureAcceptorService;
import com.test.servlet.TestSimpleServlet;


public class ClientLauncher {
	
	private TCPConnector connector = new TCPConnector();

	public TCPConnector getTCPConnector(final ApplicationContext applicationContext) throws Exception {

		try {
			
			PropertiesLoader.load();
			
			SharedBundle bundle = SharedBundle.instance();
			
			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);
			
			NIOContext context = new DefaultNIOContext();
			
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
			
			connector.setContext(context);
			
			connector.connect();
			
			return connector;

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(ClientLauncher.class).error(e.getMessage(), e);
			
			CloseUtil.close(connector);
			
			return connector;
		}
	}

	public static void main(String[] args) throws Exception {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		applicationContext.listen(TestSimpleServlet.SERVICE_NAME, new FutureAcceptorService() {
			
			public void accept(Session session, ReadFuture future) throws Exception {
				System.out.println("_________________________"+future.getText());
			}
		});
		
		ClientLauncher launcher = new ClientLauncher();

		IOConnector connector = launcher.getTCPConnector(applicationContext);
		
		FixedIOSession session = new FixedIOSession(connector.getSession());
		
		session.request(TestSimpleServlet.SERVICE_NAME, "test");

		CloseUtil.close(connector);
		
	}
}
