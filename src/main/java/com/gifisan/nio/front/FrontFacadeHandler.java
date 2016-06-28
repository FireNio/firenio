package com.gifisan.nio.front;

import java.io.IOException;

import com.gifisan.nio.acceptor.IOAcceptor;
import com.gifisan.nio.acceptor.TCPAcceptor;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class FrontFacadeHandler extends IOEventHandleAdaptor {

	private Logger				logger			= LoggerFactory.getLogger(FrontFacadeHandler.class);
	private IOAcceptor			frontProxyAcceptor	= null;
	private FrontProxyHandler	frontProxyHandler	= null;
	private byte[]				V			= {};

	public void acceptAlong(Session session, ReadFuture future) throws Exception {

		logger.info("报文来自客户端：[ {} ]，报文：{}",session.getRemoteSocketAddress(),future);
		
		Session routerSession = frontProxyHandler.getSession(session);

		if (routerSession == null) {
			
			logger.info("报文分发失败：{} ",future);
			return;
		}

		Integer sessionID = session.getSessionID();

		String transCode = future.getServiceName();

		if ("E001".equals(transCode)) {
			session.setAttribute(FrontProxyHandler.RECEIVE_BROADCAST, V);
		}

		synchronized (routerSession) {
			routerSession.setAttribute(sessionID, session);//FIXME prefix + session id
		}

		ReadFuture readFuture = ReadFutureFactory.create(routerSession,sessionID, future.getServiceName());
		
		readFuture.write(future.getText());

		routerSession.flush(readFuture);

		logger.info("分发请求到：[ {} ]", routerSession.getRemoteSocketAddress());
	}

	public void dispose() {
		this.frontProxyAcceptor.unbind();
	}

	public void startProxyServer(IOAcceptor facadeAcceptor) throws IOException {
		
		SharedBundle bundle = SharedBundle.instance();
		
		int FRONT_PORT = bundle.getIntegerProperty("FRONT.PORT", 8800);

		DebugUtil.setEnableDebug(true);

		frontProxyAcceptor = new TCPAcceptor();

		NIOContext context = new DefaultNIOContext();

		frontProxyHandler = new FrontProxyHandler(facadeAcceptor);

		FrontProxySEListener proxySEListener = new FrontProxySEListener(frontProxyHandler.getRouterProxy());

		context.setIOEventHandleAdaptor(frontProxyHandler);

		context.addSessionEventListener(proxySEListener);
		
		ServerConfiguration serverConfiguration = new ServerConfiguration();
		
		serverConfiguration.setSERVER_TCP_PORT(FRONT_PORT);
		
		context.setServerConfiguration(serverConfiguration);

		frontProxyAcceptor.setContext(context);

		frontProxyAcceptor.bind();

		logger.info("Front Proxy 启动成功 ...");
	}

}
