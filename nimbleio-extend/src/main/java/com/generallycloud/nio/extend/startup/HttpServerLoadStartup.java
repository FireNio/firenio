package com.generallycloud.nio.extend.startup;

import java.io.File;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.ChannelBufferOutputstream;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.http11.ServerHTTPProtocolFactory;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.extend.IOAcceptorUtil;

public class HttpServerLoadStartup {

	public static void main(String[] args) throws Exception {
		
		String classPath = SharedBundle.instance().getClassPath()  + "http/";
		
		File f = new File(classPath);
		
		if (f.exists()) {
			SharedBundle.instance().setClassPath(classPath);
		}
		
		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				this.acceptAlong(session, future);
			}

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				HttpReadFuture f = (HttpReadFuture) future;

				String res;

				if (f.hasOutputStream()) {

					if (f.getOutputStream() == null) {
						future.setOutputStream(new ChannelBufferOutputstream());
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
