package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceFacadeConnectorHandler extends IoEventHandleAdaptor {

	private Logger				logger	= LoggerFactory.getLogger(BalanceFacadeConnectorHandler.class);
	private FrontRouter			frontRouter;
	private FrontFacadeAcceptor	frontFacadeAcceptor;

	public BalanceFacadeConnectorHandler(FrontContext frontContext) {
		this.frontRouter = frontContext.getFrontRouter();
		this.frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {

			frontFacadeAcceptor.getAcceptor().broadcast(f);

			logger.info("广播报文：F：{}，报文：{}", session.getRemoteSocketAddress(), future);

			return;
		}

		int sessionID = f.getClientSessionID();

		SocketSession response = frontRouter.getClientSession(sessionID);

		if (response == null || response.isClosed()) {

			logger.info("连接丢失：F：{}，报文：{}", session.getRemoteSocketAddress(), future);
			return;

		}

		response.flush(f.translate());

		logger.info("回复报文：F：[{}]，T：[{}]，报文：{}",
				new Object[] { session.getRemoteSocketAddress(), response.getRemoteSocketAddress(), f });
	}

	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		String msg = future.toString();

		if (msg.length() > 100) {
			msg = msg.substring(0, 100);
		}

		logger.error("exceptionCaught,msg=" + msg, cause);
	}

}
