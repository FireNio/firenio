package test.front;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.configuration.ServerConfiguration;
import com.gifisan.nio.front.FrontContext;

public class TestFrontLoad {

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				
				NIOReadFuture readFuture = (NIOReadFuture)future;
				
				if (FrontContext.FRONT_CHANNEL_LOST.equals(readFuture.getServiceName())) {
					System.out.println("客户端已下线：" + readFuture.getText());
				} else {
					System.out.println("~~~~~~收到报文：" + future.toString());
					String res = "(***" + readFuture.getText() + "***)";
					System.out.println("~~~~~~处理报文：" + res);
					future.write(res);
					session.flush(future);
				}
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(8800);

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.connect();
	}

}
