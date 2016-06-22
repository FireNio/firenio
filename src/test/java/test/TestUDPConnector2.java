package test;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.UDPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.rtp.client.RTPClient;

public class TestUDPConnector2 {

	public static void main(String[] args) throws Exception {

		final TCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		connector.login("udp2", "udp2");

		ClientSession session = connector.getClientSession();
		
		UDPConnector udpConnector = new UDPConnector(session);
		
		udpConnector.connect();
		
		udpConnector.bindSession();
		
		RTPClient client = new RTPClient(session,udpConnector);
		
		client.setRTPHandle(new TestUDPReceiveHandle());

		ThreadUtil.sleep(99999500);
		CloseUtil.close(connector);

	}
}
