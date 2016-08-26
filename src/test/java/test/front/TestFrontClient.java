package test.front;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.IOConnectorUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TestFrontClient {

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				
				NIOReadFuture f = (NIOReadFuture)future;
				
				System.out.println(f.getText());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8600);

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.connect();

		Session session = connector.getSession();

		for (int i = 0; i < 1; i++) {

			ReadFuture future = ReadFutureFactory.create(session, "~~~service-name~~~");

			future.write("你好！");

			session.flush(future);
		}

		ThreadUtil.sleep(200);

		CloseUtil.close(connector);
	}

}
