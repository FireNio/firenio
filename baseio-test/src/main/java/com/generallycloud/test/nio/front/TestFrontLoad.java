package com.generallycloud.test.nio.front;

import com.generallycloud.nio.balance.FrontContext;
import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestFrontLoad {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				BaseReadFuture f = (BaseReadFuture)future;
				
				if (FrontContext.FRONT_CHANNEL_LOST.equals(f.getFutureName())) {
					System.out.println("客户端已下线：" + f.getReadText());
				} else {
					System.out.println("收到报文：" + future.toString());
					String res = "_____________" + f.getReadText();
					System.out.println("处理报文：" + res);
					f.write(res);
					session.flush(future);
				}
			}
		};

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_PORT(8800);

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor, configuration);

		connector.getContext().setProtocolFactory(new BaseProtocolFactory());
		
		connector.connect();
	}

}
