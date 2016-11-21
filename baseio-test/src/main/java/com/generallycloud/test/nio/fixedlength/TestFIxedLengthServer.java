package com.generallycloud.test.nio.fixedlength;

import java.io.File;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FLBeatFutureFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
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

public class TestFIxedLengthServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture) future;
				String res = "yes server already accept your message:" + f.getReadText();
				f.write(res);
				session.flush(future);
			}
		};
		
		SocketChannelAcceptor acceptor = new SocketChannelAcceptor();
		
		ServerConfiguration configuration = new ServerConfiguration(18300);
		
		BaseContext context = new BaseContextImpl(configuration);
		
		context.addSessionEventListener(new LoggerSEListener());
		
//		context.addSessionEventListener(new SessionAliveSEListener());

		context.setIOEventHandleAdaptor(eventHandleAdaptor);
		
		context.setBeatFutureFactory(new FLBeatFutureFactory());

		context.setProtocolFactory(new FixedLengthProtocolFactory());

		File certificate = SharedBundle.instance().loadFile("nio/conf/generallycloud.com.crt");
		File privateKey = SharedBundle.instance().loadFile("nio/conf/generallycloud.com.key");

		SslContext sslContext = SSLUtil.initServer(privateKey,certificate);
		
//		context.setSslContext(sslContext);
		
		acceptor.setContext(context);

		acceptor.bind();
	}
}
