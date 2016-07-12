package test.http11;

import test.ServerUtil;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.http11.HTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.HTTPReadFuture;

public class TestHttpServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				this.acceptAlong(session, future);
			}

			public void acceptAlong(Session session, ReadFuture future) throws Exception {
				HTTPReadFuture f = (HTTPReadFuture) future;

				String res;

				if (f.hasOutputStream()) {

					if (f.getOutputStream() == null) {
						f.setOutputStream(new BufferedOutputStream());
						return;
					}

					BufferedOutputStream outputStream = (BufferedOutputStream) f.getOutputStream();

					res = "yes server already accept your message :) </BR>" + outputStream.toString();
				} else {
					res = "yes server already accept your message :) " + f.getParamString();
				}

				f.write(res);
				session.flush(f);
			}
			
			
		};

		TCPAcceptor acceptor = ServerUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.getContext().setProtocolFactory(new HTTPProtocolFactory());

		acceptor.bind();
	}
}
