/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
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
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.ssl.SSLUtil;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoConnectorUtil;

public class TestSimpleWebSocketClient {

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("http");

		IoEventHandleAdaptor adaptor = new IoEventHandleAdaptor() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				if (future instanceof ClientHttpReadFuture) {
					ClientHttpReadFuture f = (ClientHttpReadFuture) future;
					if (f.getRequestHeader("Sec-WebSocket-Accept") != null) {
						f.updateWebSocketProtocol();
						WebSocketReadFuture f2 = new WebSocketReadFutureImpl(session.getContext());
						f2.write("{action: \"add-user\", username: \"火星人\"}");
//						f2.write("{\"action\":999}");
						session.flush(f2);
						
					}
					System.out.println(f.getRequestHeaders());
				} else {
					WebSocketReadFuture f = (WebSocketReadFuture) future;
					System.out.println(f.getReadText());
				}
			}
		};
		

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(adaptor);
		connector.getContext().setBeatFutureFactory(new WebSocketBeatFutureFactory());
		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		connector.getContext().setSslContext(SSLUtil.initClient());
		ServerConfiguration configuration = connector.getContext().getServerConfiguration();
		configuration.setSERVER_HOST("47.89.30.77");
//		configuration.setSERVER_HOST("120.76.222.210");
//		configuration.setSERVER_HOST("115.29.193.48");
//		configuration.setSERVER_HOST("workerman.net");
		configuration.setSERVER_PORT(7680);
//		configuration.setSERVER_PORT(30005);
//		configuration.setSERVER_PORT(29000);
//		configuration.setSERVER_PORT(8280);
		SocketSession session = connector.connect();
		String url = "/web-socket-chat";
		 url = "/c1020";
		HttpReadFuture future = new WebSocketUpgradeRequestFuture(session.getContext(),url);
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
