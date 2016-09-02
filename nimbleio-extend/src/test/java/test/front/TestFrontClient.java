package test.front;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestFrontClient {

	public static void main(String[] args) throws Exception {
		
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

			ReadFuture future = ReadFutureFactory.create(session, "service-name");

			future.write("你好！");

			session.flush(future);
		}

		ThreadUtil.sleep(200);

		CloseUtil.close(connector);
	}

}
