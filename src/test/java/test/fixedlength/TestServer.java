package test.fixedlength;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.IOAcceptorUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.fixedlength.FixedLengthProtocolFactory;
import com.gifisan.nio.component.protocol.fixedlength.future.FixedLengthReadFuture;

public class TestServer {

	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.setBasepath("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				FixedLengthReadFuture f = (FixedLengthReadFuture)future;
				String res = "yes server already accept your message:" + f.getText();
				future.write(res);
				session.flush(future);
			}
		};

		TCPAcceptor acceptor = IOAcceptorUtil.getTCPAcceptor(eventHandleAdaptor);
		
		acceptor.getContext().setProtocolFactory(new FixedLengthProtocolFactory());

		acceptor.bind();
	}
}
