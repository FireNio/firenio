package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.base.BaseProtocolFactory;
import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.IOAcceptorUtil;
import com.generallycloud.nio.protocol.ReadFuture;

public class BaseServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				BaseReadFuture f = (BaseReadFuture)future;
				String res = "yes server already accept your message" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		SocketChannelAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);
		
		acceptor.getContext().setProtocolFactory(new BaseProtocolFactory());

		acceptor.bind();
	}
}
