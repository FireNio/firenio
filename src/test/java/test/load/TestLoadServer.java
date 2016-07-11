package test.load;

import test.ServerUtil;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.nio.NIOReadFuture;

public class TestLoadServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				NIOReadFuture f = (NIOReadFuture)future;
				String res = "yes server already accept your message" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		TCPAcceptor acceptor = ServerUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.bind();
	}
}
