package com.gifisan.nio.service.impl;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.AbstractNIOFilter;

public class LoggerFilter extends AbstractNIOFilter {

	private Logger	logger	= LoggerFactory.getLogger(LoggerFilter.class);

	public void accept(Session session) throws Exception {
		logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { session.getRemoteAddr(), session.getServiceName(),
				session.getRequestText() });
	}

}
