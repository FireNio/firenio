package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorHandler extends IoEventHandleAdaptor {

	private Logger					logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontInterceptor			frontInterceptor;
	private BalanceFacadeConnector	connector;

	public FrontFacadeAcceptorHandler(FrontContext context) {
		this.connector = context.getBalanceFacadeConnector();
		this.frontInterceptor = context.getFrontInterceptor();
	}

	public void accept(SocketSession session, ReadFuture future) throws Exception {

		FrontFacadeSocketSession fs = (FrontFacadeSocketSession) session;

		BalanceReadFuture f = (BalanceReadFuture) future;

		logger.info("报文来自客户端：[ {} ]，报文：{}", fs.getRemoteSocketAddress(), f);

		if (frontInterceptor.intercept(fs, f)) {
			return;
		}

		SocketSession rs = connector.getSession();

		if (rs == null) {
			logger.info("未发现负载节点，报文分发失败：{} ", f);
			return;
		}

		f.setClientSessionID(fs.getSessionID());

		f = f.translate();

		rs.flush(f);

		logger.info("分发请求到：[ {} ]", rs.getRemoteSocketAddress());
	}

}
