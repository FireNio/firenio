package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.NamedReadFuture;
import com.generallycloud.nio.extend.service.FutureAcceptorFilter;

public class LoggerFilter extends FutureAcceptorFilter {

	private Logger	logger	= LoggerFactory.getLogger(LoggerFilter.class);

	protected void accept(Session session, NamedReadFuture future) throws Exception {

		logger.info("请求IP：{}，服务名称：{}，请求内容：{}", 
				new String[] { session.getRemoteAddr(), future.getFutureName() });
	}

}
