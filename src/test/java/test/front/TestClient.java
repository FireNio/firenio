package test.front;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class TestClient {

	public static void main(String[] args) throws IOException {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				System.out.println(future.getText());
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8600);

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.connect();

		Session session = connector.getSession();

		for (int i = 0; i < 2; i++) {

			ReadFuture future = ReadFutureFactory.create(session, "~~~service-name~~~");

			future.write("你好！");

			session.flush(future);
		}

		ThreadUtil.sleep(300);

		CloseUtil.close(connector);

	}

}
