package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.FrontRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorHandler extends IOEventHandleAdaptor {

	private Logger			logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontRouter		frontRouter;
	private byte[]		V		= {};

	public FrontFacadeAcceptorHandler(FrontContext context) {
		this.frontRouter = context.getFrontRouter();
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);

		IOSession routerSession = frontRouter.getRouterSession((IOSession) session, f);

		//FIXME 是否需要设置取消接收广播
		if (f.isReceiveBroadcast()) {
			session.setAttribute(FrontContext.FRONT_RECEIVE_BROADCAST, V);
			return;
		}

		if (routerSession == null) {

			logger.info("未发现负载节点，报文分发失败：{} ", future);
			return;
		}

		synchronized (routerSession) {
			routerSession.setAttribute(f.getFutureID(), session);
		}

		IOWriteFuture writeFuture = f.translate(routerSession);

		routerSession.flush(writeFuture);

		logger.info("分发请求到：[ {} ]", routerSession.getRemoteSocketAddress());
	}

}
