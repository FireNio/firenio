package test.http11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.ServerConfiguration;
import com.generallycloud.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.component.protocol.http11.HttpIOEventHandle;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestHttpLoadConnection {

	public static void main(String[] args) throws IOException {
		
		List<TCPConnector> connectors = new ArrayList<TCPConnector>();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("www.generallycloud.com");
		configuration.setSERVER_TCP_PORT(80);
		configuration.setSERVER_WRITE_QUEUE_SIZE(4);
		
		try {
			for (int i = 0; i < 999; i++) {
				
				if (i % 100 == 0) {
					System.out.println("i__________________"+i);
				}
				
				HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();
				
				TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
				
				eventHandleAdaptor.setTCPConnector(connector);

				connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
				connector.getContext().setServerConfiguration(configuration);
				
				connector.connect();
				
				connectors.add(connector);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			for (TCPConnector connector : connectors) {
				
				CloseUtil.close(connector);
			}
		}
		

	}
}
