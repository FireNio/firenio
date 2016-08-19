package test.load;

import java.io.IOException;

import test.IOConnectorUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;

public class TestSimpleClient {

	public static void main(String[] args) throws IOException {

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
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
