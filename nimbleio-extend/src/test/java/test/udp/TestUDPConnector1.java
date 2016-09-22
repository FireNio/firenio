package test.udp;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.connector.UDPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.plugin.rtp.client.RTPClient;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.setBasepath("nio");

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

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
