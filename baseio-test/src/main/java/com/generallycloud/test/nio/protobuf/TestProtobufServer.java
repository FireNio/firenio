/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.test.nio.protobuf;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.baseio.codec.protobuf.future.ProtobufIOEventHandle;
import com.generallycloud.baseio.codec.protobuf.future.ProtobufReadFuture;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.protocol.ReadFuture;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;

public class TestProtobufServer {

	public static void main(String[] args) throws Exception {

		ProtobufIOEventHandle eventHandleAdaptor = new ProtobufIOEventHandle() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				ProtobufReadFuture f = (ProtobufReadFuture) future;
				
				SearchRequest req =  (SearchRequest) f.getMessage();

				String message = "yes server already accept your message:\n" + req;

				System.out.println(message);
				
				
				SearchRequest res = SearchRequest.newBuilder().mergeFrom(req).setQuery("query_______").build();
				
				f.writeProtobuf(res.getClass().getName(), res);
				
				session.flush(future);
			}
		};
		
		eventHandleAdaptor.regist(SearchRequest.getDefaultInstance());
		
		SocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor(context);

		context.addSessionEventListener(new LoggerSocketSEListener());

//		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIoEventHandleAdaptor(eventHandleAdaptor);

//		context.setBeatFutureFactory(new NIOBeatFutureFactory());

		context.setProtocolFactory(new ProtobufProtocolFactory());

		acceptor.bind();
	}
}
