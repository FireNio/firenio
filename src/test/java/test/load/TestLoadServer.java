package test.load;

import test.ServerUtil;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class TestLoadServer {

	public static void main(String[] args) throws Exception {

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				String res = "yes server already accept your message" + future.getText();
				future.write(res);
				session.flush(future);
			}
		};

		TCPAcceptor acceptor = ServerUtil.getTCPAcceptor(eventHandleAdaptor);

		acceptor.bind();
	}
}
