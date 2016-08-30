package com.gifisan.nio.extend.startup;

import java.io.File;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.LoggerSEListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.protocol.http11.ServerHTTPProtocolFactory;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.FixedIOEventHandle;
import com.gifisan.nio.extend.configuration.FileSystemACLoader;
import com.gifisan.nio.extend.service.FutureAcceptorHttpFilter;

public class HttpServerStartup {
	
	private Logger logger = LoggerFactory.getLogger(HttpServerStartup.class);

	public void launch() throws Exception {

		ApplicationContext applicationContext = new ApplicationContext();

		NIOContext context = new DefaultNIOContext();

		TCPAcceptor acceptor = new TCPAcceptor();

		try {

			FileSystemACLoader fileSystemACLoader = new FileSystemACLoader();

			applicationContext
					.setLastServiceFilter(new FutureAcceptorHttpFilter(applicationContext.getClassLoader()));
			applicationContext.setConfigurationLoader(fileSystemACLoader);
			applicationContext.setContext(context);

			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSEListener());

			context.setProtocolFactory(new ServerHTTPProtocolFactory());
			
			acceptor.setContext(context);

			acceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		
		String classPath = SharedBundle.instance().getClassPath()  + "http/";
		
		File f = new File(classPath);
		
		if (f.exists()) {
			SharedBundle.instance().setClassPath(classPath);
		}
		
		HttpServerStartup launcher = new HttpServerStartup();

		launcher.launch();
	}

}
