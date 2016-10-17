package com.generallycloud.nio.extend.startup;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.IOAcceptorUtil;
import com.generallycloud.nio.protocol.ReadFuture;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				this.acceptAlong(session, future);
			}

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				HttpReadFuture f = (HttpReadFuture) future;

				String res;

				if (f.hasBodyContent()) {

					byte [] array =  f.getBodyContent();

					res = "yes server already accept your message :) </BR><PRE style='font-size: 18px;color: #FF9800;'>" + new String(array)+"</PRE>";
				} else {
					res = "yes server already accept your message :) " + f.getRequestParams();
				}

				f.write(res);
				session.flush(f);
			}
		};

		SocketChannelAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.getContext().setProtocolFactory(new ServerHTTPProtocolFactory());

		acceptor.bind();
	}
}
