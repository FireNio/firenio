package test.front;

import java.io.IOException;

import test.IOConnectorUtil;

import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;
import com.gifisan.nio.front.FrontContext;

public class TestFrontBroadcast {

	public static void main(String[] args) throws IOException {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {

				NIOReadFuture f = (NIOReadFuture) future;
				
				if (FrontContext.FRONT_CHANNEL_LOST.equals(f.getServiceName())) {
					System.out.println("客户端已下线：" + f.getText());
				} else {
					System.out.println("~~~~~~收到报文：" + future.toString());
					String res = "(***" + f.getText() + "***)";
					System.out.println("~~~~~~处理报文：" + res);
					future.write(res);
					session.flush(future);
				}
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8800);

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

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
