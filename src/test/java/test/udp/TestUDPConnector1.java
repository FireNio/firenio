package test.udp;

import test.IOConnectorUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.connector.UDPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.plugin.rtp.client.RTPClient;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("udp1", "udp1");
		
		UDPConnector udpConnector = new UDPConnector(connector.getSession());
		
		udpConnector.connect();
		
		String otherCustomerID = "udp2";
		
		RTPClient client = new RTPClient(session,udpConnector);
		
		client.bindTCPSession();
		
		client.setRTPHandle(new TestUDPReceiveHandle());

		client.createRoom(otherCustomerID);

		ThreadUtil.sleep(99999500);
		CloseUtil.close(connector);

	}

}
