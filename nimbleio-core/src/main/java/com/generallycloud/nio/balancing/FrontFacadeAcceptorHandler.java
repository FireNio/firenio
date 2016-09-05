package com.generallycloud.nio.balancing;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class FrontFacadeAcceptorHandler extends IOEventHandleAdaptor {

	private Logger				logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontRouterMapping	frontRouterMapping;
	private byte[]				V		= {};
	
	public FrontFacadeAcceptorHandler(FrontRouterMapping frontRouterMapping) {
		this.frontRouterMapping = frontRouterMapping;
	}

	public void accept(Session session, ReadFuture future) throws Exception {
		
		NIOReadFuture f = (NIOReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);

		Session routerSession = frontRouterMapping.getRouterSession(session);

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
