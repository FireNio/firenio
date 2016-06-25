package com.gifisan.nio.component;

import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.NIOContext;
import com.test.servlet.TestSimpleServlet;


public class ClientLauncher {
	
	private TCPConnector connector = new TCPConnector();

	public TCPConnector getTCPConnector(IOEventHandle eventHandle) throws Exception {

		try {
			
			PropertiesLoader.load();
			
			SharedBundle bundle = SharedBundle.instance();
			
			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
			
			DebugUtil.setEnableDebug(debug);
			
			
			
			return connector;

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(ClientLauncher.class).error(e.getMessage(), e);
			
			CloseUtil.close(connector);
			
			return connector;
		}
	}

	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.load();
		
		SharedBundle bundle = SharedBundle.instance();
		
		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");
		
		DebugUtil.setEnableDebug(debug);
		
		TCPConnector connector = new TCPConnector();
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle(connector);
		
		NIOContext context = new DefaultNIOContext();
		
		context.setIOEventHandle(eventHandle);
		
		context.addSessionEventListener(new DefaultSessionEventListener());
		
		connector.setContext(context);
		
		connector.connect();

		FixedSession session = eventHandle.getFixedSession();
		
		session.listen(TestSimpleServlet.SERVICE_NAME, new OnReadFuture() {
			
			public void onResponse(FixedSession session, ReadFuture future) {
				System.out.println("_________________________"+future.getText());
			}
		});
		
		ReadFuture future = session.request(TestSimpleServlet.SERVICE_NAME, "test");
		
		System.out.println("============"+future.getText());
		
		session.write(TestSimpleServlet.SERVICE_NAME, "test");

		Thread.sleep(500);
		
		CloseUtil.close(connector);
		
	}
}
