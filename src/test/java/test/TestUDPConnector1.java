package test;

import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.UDPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.rtp.client.RTPClient;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {

		TCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();
		
		connector.login("udp1", "udp1");

		FixedSession session = connector.getClientSession();
		
		UDPConnector udpConnector = new UDPConnector(session);
		
		udpConnector.connect();
		
		String otherCustomerID = "udp2";
		
		udpConnector.bindSession();

		RTPClient client = new RTPClient(session,udpConnector);
		
		client.setRTPHandle(new TestUDPReceiveHandle());

		client.createRoom(otherCustomerID);

		ThreadUtil.sleep(99999500);
		CloseUtil.close(connector);

	}

}
