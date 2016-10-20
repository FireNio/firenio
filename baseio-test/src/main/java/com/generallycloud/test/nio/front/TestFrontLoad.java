package com.generallycloud.test.nio.front;

import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestFrontLoad {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				
				NIOReadFuture readFuture = (NIOReadFuture)future;
				
				if (FrontContext.FRONT_CHANNEL_LOST.equals(readFuture.getFutureName())) {
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

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new NIOProtocolFactory());
		
		connector.connect();
	}

}
