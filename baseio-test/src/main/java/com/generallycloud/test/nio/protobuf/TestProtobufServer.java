package com.generallycloud.test.nio.protobuf;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.protobuf.ProtobufProtocolFactory;
import com.generallycloud.nio.codec.protobuf.future.ProtobufIOEventHandle;
import com.generallycloud.nio.codec.protobuf.future.ProtobufReadFuture;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.protobuf.TestProtoBufBean.SearchRequest;

public class TestProtobufServer {

	public static void main(String[] args) throws Exception {

		ProtobufIOEventHandle eventHandleAdaptor = new ProtobufIOEventHandle() {

			@Override
			public void accept(Session session, ReadFuture future) throws Exception {
				
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

		ServerConfiguration configuration = new ServerConfiguration();

		configuration.setSERVER_TCP_PORT(18300);

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		BaseContext context = new BaseContextImpl(configuration);

		context.addSessionEventListener(new LoggerSEListener());

//		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);

//		context.setBeatFutureFactory(new NIOBeatFutureFactory());

		context.setProtocolFactory(new ProtobufProtocolFactory());

		acceptor.setContext(context);

		acceptor.bind();
	}
}
