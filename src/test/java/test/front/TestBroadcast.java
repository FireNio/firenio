package test.front;

import java.io.IOException;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TestBroadcast {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {

		PropertiesLoader.load();

		TCPConnector connector = new TCPConnector();

		SharedBundle bundle = SharedBundle.instance();

		boolean debug = bundle.getBooleanProperty("SERVER.DEBUG");

		DebugUtil.setEnableDebug(debug);

		NIOContext context = new DefaultNIOContext();

		context.setIOEventHandleAdaptor(new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				System.out.println("~~~~~~收到报文：" + future.getText());
				String res = "(***" + future.getText() + "***)";
				System.out.println("~~~~~~处理报文：" + res);
				future.write(res);
				session.flush(future);
			}
		});

		context.addSessionEventListener(new LoggerSEtListener());

		ServerConfiguration configuration = new ServerConfiguration();
		configuration.setSERVER_TCP_PORT(8800);
		context.setServerConfiguration(configuration);

		connector.setContext(context);

		connector.connect();

		Session session = connector.getSession();

		for (;;) {

			ReadFuture future = ReadFutureFactory.create(session, "broadcast");

			future.write("broadcast msg");

			session.flush(future);

			ThreadUtil.sleep(2000);
		}
	}

}
