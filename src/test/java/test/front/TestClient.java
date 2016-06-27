package test.front;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.DefaultSessionEventListener;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TestClient {

	public static void main(String[] args) throws IOException {

		PropertiesLoader.load();
		
		TCPConnector connector = new TCPConnector();

		SharedBundle bundle = SharedBundle.instance();

		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");

		DebugUtil.setEnableDebug(debug);

		NIOContext context = new DefaultNIOContext();

		context.setIOEventHandleAdaptor(new IOEventHandleAdaptor() {
			
			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				System.out.println(future.getText());
			}
		});

		context.addSessionEventListener(new DefaultSessionEventListener());
		
		ServerConfiguration configuration = new ServerConfiguration();
		configuration.setSERVER_TCP_PORT(8600);
		context.setServerConfiguration(configuration);

		connector.setContext(context);
		
		connector.connect();
		
		Session session = connector.getSession();
		
		for (int i = 0; i < 5; i++) {
			
			ReadFuture future = ReadFutureFactory.create(session,"~~~service-name~~~");
			
			future.write("你好！");
			
			session.flush(future);
		}
		
		ThreadUtil.sleep(300);
		
		CloseUtil.close(connector);
		
	}

}
