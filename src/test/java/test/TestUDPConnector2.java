package test;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.connector.UDPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.plugin.rtp.client.RTPClient;

public class TestUDPConnector2 {

	public static void main(String[] args) throws Exception {

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("udp2", "udp2");
		
		UDPConnector udpConnector = new UDPConnector(connector.getSession());
		
		udpConnector.connect();
		
		RTPClient client = new RTPClient(session,udpConnector);
		
		client.bindTCPSession();
		
		client.setRTPHandle(new TestUDPReceiveHandle());

		ThreadUtil.sleep(99999500);
		CloseUtil.close(connector);

	}
}
