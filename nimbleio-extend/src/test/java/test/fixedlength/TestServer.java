package test.fixedlength;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.component.protocol.fixedlength.future.FixedLengthReadFuture;
import com.generallycloud.nio.extend.IOAcceptorUtil;

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
