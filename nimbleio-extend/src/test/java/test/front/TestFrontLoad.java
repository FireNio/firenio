package test.front;

import java.io.IOException;

import com.generallycloud.nio.balancing.FrontContext;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestFrontLoad {

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				
				NIOReadFuture readFuture = (NIOReadFuture)future;
				
				if (FrontContext.FRONT_CHANNEL_LOST.equals(readFuture.getServiceName())) {
					System.out.println("客户端已下线：" + readFuture.getText());
				} else {
					System.out.println("收到报文：" + future.toString());
					String res = "_____________" + readFuture.getText();
					System.out.println("处理报文：" + res);
					future.write(res);
					session.flush(future);
				}
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8800);

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.connect();
	}

}
