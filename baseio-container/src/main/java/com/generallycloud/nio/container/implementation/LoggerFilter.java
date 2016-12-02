package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;

public class LoggerFilter extends FutureAcceptorFilter {

	private Logger logger = LoggerFactory.getLogger(LoggerFilter.class);

	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		logger.info("请求IP：{}，服务名称：{}，请求内容：{}",
				new String[] { session.getRemoteAddr(), future.getFutureName(), future.getReadText() });
	}

}
