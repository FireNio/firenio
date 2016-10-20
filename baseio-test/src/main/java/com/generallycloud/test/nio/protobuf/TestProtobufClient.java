package com.generallycloud.test.nio.protobuf;

import com.generallycloud.nio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.nio.codec.protobuf.future.ProtobufIOEventHandle;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFuture;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFutureImpl;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.ConnectorCloseSEListener;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest.Corpus;
import com.google.protobuf.ByteString;

public class TestProtobufClient {

	public static void main(String[] args) throws Exception {

		ProtobufIOEventHandle eventHandleAdaptor = new ProtobufIOEventHandle() {

			@Override
			public void accept(Session session, ReadFuture future) throws Exception {
				
				ProtobufReadFuture f = (ProtobufReadFuture) future;
				
				SearchRequest res =  (SearchRequest) f.getMessage();

				System.out.println();
				System.out.println("________"+res);
				System.out.println();
			}
		};
		
		eventHandleAdaptor.regist(SearchRequest.getDefaultInstance());

		SocketChannelConnector connector = new SocketChannelConnector();
		
		ServerConfiguration configuration = new ServerConfiguration();
		
		configuration.setSERVER_HOST("localhost");
		configuration.setSERVER_TCP_PORT(18300);
		
		BaseContext context = new BaseContextImpl(configuration);

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.addSessionEventListener(new LoggerSEListener());

		context.addSessionEventListener(new ConnectorCloseSEListener(connector));

//		context.addSessionEventListener(new SessionActiveSEListener());
		
//		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new ProtobufProtocolFactory());
		
		connector.setContext(context);
		
		Session session = connector.connect();
		
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
