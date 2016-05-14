package test;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientUDPConnector;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class TestUDPConnector {
	
	
	public static void main(String[] args) throws Exception {


		String param = ClientUtil.getParamString();
		
		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();

		ClientSession session = connector.getClientSession();
		
		ClientUDPConnector udpConnector = new ClientUDPConnector(session) ;
		
		DatagramPacket packet = new DatagramPacket(param.getBytes());
		
		udpConnector.sendDatagramPacket(packet);
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		ThreadUtil.sleep(500);
		CloseUtil.close(udpConnector);
		CloseUtil.close(connector);
		
	}
}
