package com.gifisan.nio.extend.startup;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.IOAcceptorUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.ServerHTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				this.acceptAlong(session, future);
			}

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				HttpReadFuture f = (HttpReadFuture) future;

				String res;

				if (f.hasOutputStream()) {

					if (f.getOutputStream() == null) {
						f.setOutputStream(new BufferedOutputStream());
						return;
					}

					BufferedOutputStream outputStream = (BufferedOutputStream) f.getOutputStream();

					res = "yes server already accept your message :) </BR><PRE style='font-size: 18px;color: #FF9800;'>" + outputStream.toString()+"</PRE>";
				} else {
					res = "yes server already accept your message :) " + f.getRequestParams();
				}

				f.write(res);
				session.flush(f);
			}
		};

		TCPAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.getContext().setProtocolFactory(new ServerHTTPProtocolFactory());

		acceptor.bind();
	}
}
