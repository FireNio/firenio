package test;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.plugin.rtp.client.RTPClient;

public class TestUDPConnector1 {

	public static void main(String[] args) throws Exception {

		final ClientTCPConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		ClientSession session = connector.getClientSession();

		final String customerID = "001";

		final String otherCustomerID = "002";

		final RTPClient client = new RTPClient(session, new TestUDPReceiveHandle(customerID, otherCustomerID),
				customerID);

		connector.login("admin", "admin100");

		client.createRoom(otherCustomerID);

		ThreadUtil.sleep(99999500);
		CloseUtil.close(client);
		CloseUtil.close(connector);

	}

}
