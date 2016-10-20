package com.generallycloud.test.nio.load;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.IOAcceptorUtil;
import com.generallycloud.nio.protocol.ReadFuture;

public class TestLoadServer {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		final AtomicInteger res = new AtomicInteger();
		final AtomicInteger req = new AtomicInteger();

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				NIOReadFuture f = (NIOReadFuture)future;
				String res = "yes server already accept your message" + f.getText();
				future.write(res);
				session.flush(future);
				System.out.println("req======================"+req.getAndIncrement());
			}
			
			public void futureSent(Session session, ReadFuture future) {
//				NIOReadFuture f = (NIOReadFuture) future;
//				System.out.println(f.getWriteBuffer());
				System.out.println("res==========="+res.getAndIncrement());
			}
		};

		SocketChannelAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);
		
		acceptor.getContext().setProtocolFactory(new NIOProtocolFactory());

		acceptor.bind();
	}
}
