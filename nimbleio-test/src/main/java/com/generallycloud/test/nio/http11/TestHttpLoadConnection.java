package com.generallycloud.test.nio.http11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestHttpLoadConnection {

	public static void main(String[] args) throws IOException {
		
		SharedBundle.instance().loadAllProperties("http");
		
		List<SocketChannelConnector> connectors = new ArrayList<SocketChannelConnector>();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("www.generallycloud.com");
		configuration.setSERVER_TCP_PORT(80);
		configuration.setSERVER_CHANNEL_QUEUE_SIZE(4);
		
		try {
			for (int i = 0; i < 999; i++) {
				
				if (i % 100 == 0) {
					System.out.println("i__________________"+i);
				}
				
				HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();
				
				SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor,configuration);
				
				eventHandleAdaptor.setTCPConnector(connector);

				connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
				
				connector.connect();
				
				connectors.add(connector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			for (SocketChannelConnector connector : connectors) {
				
				CloseUtil.close(connector);
			}
		}
	}
}
