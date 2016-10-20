package com.generallycloud.test.nio.udp;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.DatagramChannelConnector;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.rtp.client.RTPClient;

public class TestUDPConnector2 {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("udp2", "udp2");
		
		DatagramChannelConnector udpConnector = new DatagramChannelConnector(connector.getSession());
		
		udpConnector.connect();
		
		RTPClient client = new RTPClient(session,udpConnector);
		
		client.bindTCPSession();
		
		client.setRTPHandle(new TestUDPReceiveHandle());

		ThreadUtil.sleep(99999500);
		CloseUtil.close(connector);

	}
}
