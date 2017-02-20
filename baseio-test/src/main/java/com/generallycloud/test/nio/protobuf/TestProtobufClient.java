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

import com.generallycloud.nio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.nio.codec.protobuf.future.ProtobufIOEventHandle;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFuture;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.LoggerSocketSEListener;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest.Corpus;
import com.google.protobuf.ByteString;

public class TestProtobufClient {

	public static void main(String[] args) throws Exception {

		ProtobufIOEventHandle eventHandleAdaptor = new ProtobufIOEventHandle() {

			@Override
			public void accept(SocketSession session, ReadFuture future) throws Exception {
				
				ProtobufReadFuture f = (ProtobufReadFuture) future;
				
				SearchRequest res =  (SearchRequest) f.getMessage();

				System.out.println();
				System.out.println("________"+res);
				System.out.println();
			}
		};
		
		eventHandleAdaptor.regist(SearchRequest.getDefaultInstance());
		
		NioSocketChannelContext context = new NioSocketChannelContext(new ServerConfiguration(18300));

		SocketChannelConnector connector = new SocketChannelConnector(context);

		context.setIoEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSocketSEListener());

//		context.addSessionEventListener(new SessionActiveSEListener());
		
//		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new ProtobufProtocolFactory());
		
		SocketSession session = connector.connect();
		
		ProtobufReadFuture f = new ProtobufReadFutureImpl(context,"test-protobuf");

		ByteString byteString = ByteString.copyFrom("222".getBytes());
		
		SearchRequest request = SearchRequest
				.newBuilder()
				.setCorpus(Corpus.IMAGES)
				.setPageNumber(100)
				.setQuery("test")
				.setQueryBytes(byteString)
				.setResultPerPage(-1)
				.build();

		f.writeProtobuf(request);

		session.flush(f);
		
		ThreadUtil.sleep(100);

		CloseUtil.close(connector);

	}
}
