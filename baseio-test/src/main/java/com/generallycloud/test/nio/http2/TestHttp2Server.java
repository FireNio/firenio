package com.generallycloud.test.nio.http2;

import java.io.File;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http2.Http2ProtocolFactory;
import com.generallycloud.nio.codec.http2.Http2SessionFactory;
import com.generallycloud.nio.codec.http2.future.Http2FrameHeader;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.ssl.SSLUtil;
import com.generallycloud.nio.common.ssl.SslContext;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.BaseContextImpl;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestHttp2Server {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("http");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				Http2FrameHeader f = (Http2FrameHeader) future;
				System.out.println(f);
				String res = "yes server already accept your message:";
				future.write(res);
				session.flush(future);
			}
		};

		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();

		BaseContext context = new BaseContextImpl(new ServerConfiguration(443));

		context.addSessionEventListener(new LoggerSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);

		context.setProtocolFactory(new Http2ProtocolFactory());
		
		context.setSessionFactory(new Http2SessionFactory());

		File certificate = SharedBundle.instance().loadFile("nio/conf/generallycloud.com.crt");
		File privateKey = SharedBundle.instance().loadFile("nio/conf/generallycloud.com.key");

		SslContext sslContext = SSLUtil.initServer(privateKey, certificate);

		context.setSslContext(sslContext);

		acceptor.setContext(context);

		acceptor.bind();

	}

}
