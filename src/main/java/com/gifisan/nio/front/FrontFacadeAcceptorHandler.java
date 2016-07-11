package com.gifisan.nio.front;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.nio.NIOReadFuture;

public class FrontFacadeAcceptorHandler extends IOEventHandleAdaptor {

	private Logger				logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontRouterMapping	frontRouterMapping;
	private byte[]				V		= {};
	
	public FrontFacadeAcceptorHandler(FrontRouterMapping frontRouterMapping) {
		this.frontRouterMapping = frontRouterMapping;
	}

	public void acceptAlong(Session session, ReadFuture future) throws Exception {
		
		NIOReadFuture f = (NIOReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);

		Session routerSession = frontRouterMapping.getSession(session);

		if (routerSession == null) {

			logger.info("未发现负载节点，报文分发失败：{} ", future);
			return;
		}

		Integer sessionID = session.getSessionID();

		String transCode = f.getServiceName();

		if ("E001".equals(transCode)) {
			session.setAttribute(FrontContext.FRONT_RECEIVE_BROADCAST, V);
		}

		ReadFuture readFuture = ReadFutureFactory.create(routerSession, sessionID, f.getServiceName());

		readFuture.write(f.getText());

		routerSession.flush(readFuture);

		logger.info("分发请求到：[ {} ]", routerSession.getRemoteSocketAddress());
	}

}
