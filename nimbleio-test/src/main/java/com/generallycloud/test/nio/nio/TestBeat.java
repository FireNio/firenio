package com.generallycloud.test.nio.nio;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.SessionActiveSEListener;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOBeatFutureFactory;
import com.generallycloud.nio.configuration.PropertiesSCLoader;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.configuration.ServerConfigurationLoader;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.test.service.nio.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();
		
		ServerConfigurationLoader configurationLoader = new PropertiesSCLoader();
		
		ServerConfiguration configuration = configurationLoader.loadConfiguration(SharedBundle.instance());

		configuration.setSERVER_SESSION_IDLE_TIME(100);
		
		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle,configuration);
		
		connector.getContext().addSessionEventListener(new SessionActiveSEListener());
		
		connector.getContext().setBeatFutureFactory(new NIOBeatFutureFactory());
		
		FixedSession session = eventHandle.getFixedSession();
		
		connector.connect();
		
		session.login("admin", "admin100");
		
		String param = "tttt";
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 5; i++) {
		
			ReadFuture future = session.request(serviceKey, param);
			System.out.println(future);
			ThreadUtil.sleep(1000);
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
		
	}
}
