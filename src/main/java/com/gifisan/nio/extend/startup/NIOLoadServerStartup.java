package com.gifisan.nio.extend.startup;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.IOAcceptorUtil;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class NIOLoadServerStartup {

	public static void main(String[] args) throws Exception {
		
		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				NIOReadFuture f = (NIOReadFuture)future;
				String res = "yes server already accept your message" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		TCPAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.bind();
	}
}
