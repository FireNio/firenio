package test.load;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestSimpleClient {

	public static void main(String[] args) throws Exception {

		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				System.out.println(future);
			}
		};

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.connect();

		Session session = connector.getSession();

		ReadFuture future = ReadFutureFactory.create(session, "test", session.getContext().getIOEventHandleAdaptor());

		future.write("hello server !");

		session.flush(future);
		
		ThreadUtil.sleep(500);

		CloseUtil.close(connector);

	}
}
