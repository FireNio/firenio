package test.http11;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.acceptor.UDPAcceptor;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEtListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.protocol.http11.ServerHTTPProtocolFactory;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.FixedIOEventHandle;
import com.gifisan.nio.extend.configuration.FileSystemACLoader;
import com.gifisan.nio.extend.service.FutureAcceptorHttpFilter;


public class TestHTTPServer {

	public void launch() throws Exception {
		
		ApplicationContext applicationContext = new ApplicationContext();
		
		NIOContext context = new DefaultNIOContext();
		
		TCPAcceptor acceptor = new TCPAcceptor();
		
		UDPAcceptor udpAcceptor = new UDPAcceptor();
		
		try {
			
			FileSystemACLoader fileSystemACLoader = new FileSystemACLoader();
			
			fileSystemACLoader.setBasePath("http");
			
			applicationContext.setLastServiceFilter(new FutureAcceptorHttpFilter(applicationContext.getClassLoader()));
			applicationContext.setConfigurationLoader(fileSystemACLoader);
			applicationContext.setContext(context);
			
			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));
			
			context.addSessionEventListener(new LoggerSEtListener());
			
			context.setProtocolFactory(new ServerHTTPProtocolFactory());
			
			acceptor.setContext(context);
			
			acceptor.bind();
			
			udpAcceptor.setContext(context);
			
			udpAcceptor.bind();

		} catch (Throwable e) {
			
			LoggerFactory.getLogger(TestHTTPServer.class).error(e.getMessage(), e);
			
			LifeCycleUtil.stop(applicationContext);
			
			acceptor.unbind();
			
			udpAcceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		TestHTTPServer launcher = new TestHTTPServer();

		launcher.launch();
	}
}
