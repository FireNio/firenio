package test.fixedlength;

import java.io.IOException;

import test.IOConnectorUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.fixedlength.FixedLengthProtocolFactory;
import com.gifisan.nio.component.protocol.fixedlength.future.FixedLengthReadFuture;
import com.gifisan.nio.component.protocol.fixedlength.future.FixedLengthReadFutureImpl;
import com.gifisan.nio.connector.TCPConnector;

public class TestClient {

	public static void main(String[] args) throws IOException {

		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {

				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				System.out.println();
				System.out.println("____________________"+f.getText());
				System.out.println();
			}
		};

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.getContext().setProtocolFactory(new FixedLengthProtocolFactory());

		connector.connect();

		Session session = connector.getSession();

		ReadFuture future = new FixedLengthReadFutureImpl(session);

		future.write("hello server !");

		session.flush(future);

		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
