package com.generallycloud.nio.extend.startup;

import java.io.File;

import com.generallycloud.nio.acceptor.TCPAcceptor;
import com.generallycloud.nio.acceptor.UDPAcceptor;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.DefaultNIOContext;
import com.generallycloud.nio.component.LoggerSEListener;
import com.generallycloud.nio.component.ManagerSEListener;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.protocol.nio.NIOProtocolFactory;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.FixedIOEventHandle;
import com.generallycloud.nio.extend.configuration.FileSystemACLoader;

public class NIOServerStartup {

	private Logger		logger	= LoggerFactory.getLogger(NIOServerStartup.class);

	public void launch() throws Exception {
		
		ApplicationContext applicationContext = new ApplicationContext();

		NIOContext context = new DefaultNIOContext();

		TCPAcceptor acceptor = new TCPAcceptor();

		UDPAcceptor udpAcceptor = new UDPAcceptor();

		try {

			FileSystemACLoader fileSystemACLoader = new FileSystemACLoader();

			applicationContext.setConfigurationLoader(fileSystemACLoader);
			applicationContext.setContext(context);

			context.setIOEventHandleAdaptor(new FixedIOEventHandle(applicationContext));

			context.addSessionEventListener(new LoggerSEListener());

			context.addSessionEventListener(new ManagerSEListener());

			context.setProtocolFactory(new NIOProtocolFactory());
			
			context.setAcceptBeat(true);

			acceptor.setContext(context);

			acceptor.bind();

			udpAcceptor.setContext(context);

			udpAcceptor.bind();

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			LifeCycleUtil.stop(applicationContext);

			acceptor.unbind();

			udpAcceptor.unbind();
		}
	}

	public static void main(String[] args) throws Exception {
		
		String classPath = SharedBundle.instance().getClassPath()  + "nio/";
		
		File f = new File(classPath);
		
		if (f.exists()) {
			SharedBundle.instance().setClassPath(classPath);
		}
		
		NIOServerStartup launcher = new NIOServerStartup();

		launcher.launch();
	}
}
