package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.ClientHttpReadFuture;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketBeatFutureFactory;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFuture;
import com.generallycloud.nio.codec.http11.future.WebSocketReadFutureImpl;
import com.generallycloud.nio.codec.http11.future.WebSocketUpgradeRequestFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestSimpleWebSocketClient {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("http");

		IOEventHandleAdaptor adaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				if (future instanceof ClientHttpReadFuture) {
					ClientHttpReadFuture f = (ClientHttpReadFuture) future;
					if (f.getRequestHeader("Sec-WebSocket-Accept") != null) {
						f.updateWebSocketProtocol();
						WebSocketReadFuture f2 = new WebSocketReadFutureImpl();
						f2.write("{action: \"add-user\", username: \"火星人\"}");
//						f2.write("{\"action\":999}");
						session.flush(f2);
						
					}
					System.out.println(f.getRequestHeaders());
				} else {
					WebSocketReadFuture f = (WebSocketReadFuture) future;
					System.out.println(f.getData().toString());
				}
			}
		};

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(adaptor);
		connector.getContext().setBeatFutureFactory(new WebSocketBeatFutureFactory());
		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		ServerConfiguration configuration = connector.getContext().getServerConfiguration();
		configuration.setSERVER_HOST("120.76.222.210");
//		configuration.setSERVER_HOST("115.29.193.48");
//		configuration.setSERVER_HOST("workerman.net");
		configuration.setSERVER_TCP_PORT(30005);
//		configuration.setSERVER_TCP_PORT(29000);
//		configuration.setSERVER_TCP_PORT(8280);
		Session session = connector.connect();
		String url = "/web-socket-chat";
		 url = "/";
		HttpReadFuture future = new WebSocketUpgradeRequestFuture(url);
//		 future.setRequestURL("ws://120.76.222.210:30005/");
//		future.setResponseHeader("Host", "120.76.222.210:30005");
//		future.setResponseHeader("Pragma", "no-cache");
//		future.setResponseHeader("Cache-Control", "no-cache");
//		future.setResponseHeader("User-Agent",
//				"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
//		future.setResponseHeader("Accept-Encoding", "gzip, deflate, sdch");
//		future.setResponseHeader("Accept-Language", "zh-CN,zh;q=0.8");
		// future.setRequestHeader("", "");
		session.flush(future);
		
//		ThreadUtil.sleep(1000);
//		WebSocketReadFuture f2 = new WebSocketReadFutureImpl();
//		f2.write("test");
//		session.flush(f2);
		
		ThreadUtil.sleep(999999999);
		CloseUtil.close(connector);

	}
}
