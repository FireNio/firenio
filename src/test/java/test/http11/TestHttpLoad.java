package test.http11;

import java.io.IOException;
import java.math.BigDecimal;

import test.ClientUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpRequestFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.plugin.http.client.HttpClient;
import com.gifisan.nio.extend.plugin.http.client.HttpIOEventHandle;

public class TestHttpLoad {

	final static int		time		= 50000;
	final static Logger	logger	= LoggerFactory.getLogger(TestHttpLoad.class);

	public static void main(String[] args) throws IOException {

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandleAdaptor);

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		connector.connect();

		Session session = connector.getSession();

		System.out.println("################## Test start ####################");

		HttpClient client = eventHandleAdaptor.getHttpClient();
		
		long old = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			
			HttpRequestFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");
			
			HttpReadFuture res = client.request(session, future, 3000);
			
//			System.out.println(res.getOutputStream());
		}

		long spend = (System.currentTimeMillis() - old);
		System.out.println("## Execute Time:" + time);
		System.out.println("## OP/S:"
				+ new BigDecimal(time * 1000).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP));
		System.out.println("## Expend Time:" + spend);

		CloseUtil.close(connector);

	}
}
