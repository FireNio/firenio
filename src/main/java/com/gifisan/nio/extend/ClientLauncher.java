package com.gifisan.nio.extend;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.IOConnector;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.test.servlet.TestSimpleServlet;

public class ClientLauncher {

	private TCPConnector		connector		= new TCPConnector();

	private SimpleIOEventHandle	eventHandle	;
	
	private FixedSession fixedSession = new FixedIOSession();

	public SimpleIOEventHandle getEventHandle() {
		return eventHandle;
	}

	public TCPConnector getTCPConnector() {

		try {

			PropertiesLoader.load();

			SharedBundle bundle = SharedBundle.instance();

			boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");

			DebugUtil.setEnableDebug(debug);
			
			NIOContext context = new DefaultNIOContext();

			eventHandle = new SimpleIOEventHandle(fixedSession);

			context.setIOEventHandleAdaptor(eventHandle);

			context.addSessionEventListener(new LoggerSEtListener());
			
			context.addSessionEventListener(new UpdateFixedSessionSEListener(fixedSession));
			
			connector.setContext(context);
			
			fixedSession.listen(MergeSessionIDSEListener.MERGE_SESSION_ID_LISTENER, new UpdateSessionIDOnReadFuture());

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(ClientLauncher.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}
	}

	public FixedSession getFixedSession() {
		return fixedSession;
	}

	public static void main(String[] args) throws Exception {

		ClientLauncher launcher = new ClientLauncher();

		IOConnector connector = launcher.getTCPConnector();

		connector.connect();

		FixedSession session = launcher.getFixedSession();

		session.listen(TestSimpleServlet.SERVICE_NAME, new OnReadFuture() {

			public void onResponse(FixedSession session, ReadFuture future) {
				System.out.println("_________________________" + future.getText());
			}
		});

		ReadFuture future = session.request(TestSimpleServlet.SERVICE_NAME, "test");

		System.out.println("============" + future.getText());

		session.write(TestSimpleServlet.SERVICE_NAME, "test");

		Thread.sleep(1000);

		CloseUtil.close(connector);

	}
}
