package com.generallycloud.test.nio.load;

import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.codec.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.IoAcceptorUtil;

public class TestLoadServer {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("nio");
		
		final AtomicInteger res = new AtomicInteger();
		final AtomicInteger req = new AtomicInteger();

		IoEventHandleAdaptor eventHandleAdaptor = new IoEventHandleAdaptor() {

			public void accept(SocketSession session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture)future;
				String res = "yes server already accept your message" + f.getReadText();
				f.write(res);
				session.flush(future);
//				System.out.println("req======================"+req.getAndIncrement());
			}
			
			public void futureSent(SocketSession session, ReadFuture future) {
//				NIOReadFuture f = (NIOReadFuture) future;
//				System.out.println(f.getWriteBuffer());
//				System.out.println("res==========="+res.getAndIncrement());
			}
		};

		SocketChannelAcceptor acceptor = IoAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);
		
		acceptor.getContext().setProtocolFactory(new FixedLengthProtocolFactory());
		
		ServerConfiguration c = acceptor.getContext().getServerConfiguration();
		
		c.setSERVER_MEMORY_POOL_CAPACITY(1280000);
		c.setSERVER_MEMORY_POOL_UNIT(256);
		c.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.5);
		
		acceptor.bind();
	}
}
