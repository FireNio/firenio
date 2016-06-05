package test;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientUDPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.rtp.client.RTPClient;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {

		ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();
		
		connector.login("udp1", "udp1");

		ClientSession session = connector.getClientSession();
		
		ClientUDPConnector udpConnector = new ClientUDPConnector(session);
		
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
