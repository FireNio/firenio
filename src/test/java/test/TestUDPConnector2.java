package test;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.rtp.client.RTPClient;

public class TestUDPConnector2 {

	public static void main(String[] args) throws Exception {

		final ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		connector.login("udp2", "udp2");

		ClientSession session = connector.getClientSession();
		
		final RTPClient client = new RTPClient(session, new TestUDPReceiveHandle());

		ThreadUtil.sleep(99999500);
		CloseUtil.close(client);
		CloseUtil.close(connector);

	}
}
