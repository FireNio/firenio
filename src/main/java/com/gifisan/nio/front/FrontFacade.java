package com.gifisan.nio.front;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class FrontFacade {

	private SharedBundle		bundle		= SharedBundle.instance();
	private AtomicBoolean		started		= new AtomicBoolean(false);
	private FrontFacadeHandler	facadeHandler	= null;
	private TCPAcceptor			acceptor		= new TCPAcceptor();

	public void start() throws IOException {

		if (started.compareAndSet(false, true)) {
			
			PropertiesLoader.load();

			int FRONT_PORT = bundle.getIntegerProperty("FRONT.PORT", 8600);

			facadeHandler = new FrontFacadeHandler();

			DebugUtil.setEnableDebug(true);

			NIOContext context = new DefaultNIOContext();

			context.setIOEventHandleAdaptor(facadeHandler);

			context.addSessionEventListener(new FrontFacadeSEListener());
			
			ServerConfiguration serverConfiguration = new ServerConfiguration();
			
			serverConfiguration.setSERVER_TCP_PORT(FRONT_PORT);
			
			context.setServerConfiguration(serverConfiguration);

			acceptor.setContext(context);

			facadeHandler.startProxyServer(acceptor);
			
			acceptor.bind();

			LoggerFactory.getLogger(FrontFacade.class).info("Front Facade 启动成功 ...");
		}
	}

	public void stop() {
		if (started.get()) {
			
			acceptor.unbind();
			
			facadeHandler.dispose();
		}
	}

	public void test() {

	}

}
