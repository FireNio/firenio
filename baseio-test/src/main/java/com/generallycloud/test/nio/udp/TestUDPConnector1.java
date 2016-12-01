package com.generallycloud.test.nio.udp;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.DatagramChannelConnector;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.SimpleIOEventHandle;
import com.generallycloud.nio.container.rtp.client.RTPClient;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {
		
//		SharedBundle.instance().loadAllProperties("nio");
//
//		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();
//
//		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandle);
//
//		FixedSession session = new FixedSession(connector.connect());
//
//		session.login("udp1", "udp1");
//		
//		DatagramChannelConnector udpConnector = new DatagramChannelConnector(connector.getContext());
//		
//		udpConnector.connect();
//		
//		String otherCustomerID = "udp2";
//		
//		RTPClient client = new RTPClient(session,udpConnector);
//		
//		client.bindTCPSession();
//		
//		client.setRTPHandle(new TestUDPReceiveHandle());
//
//		client.createRoom(otherCustomerID);
//
//		ThreadUtil.sleep(99999500);
//		CloseUtil.close(connector);

	}

}
