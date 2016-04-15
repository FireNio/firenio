package com.gifisan.nio.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.service.AbstractNIOFilter;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class LoggerFilter extends AbstractNIOFilter {

	private Logger	logger	= LoggerFactory.getLogger(LoggerFilter.class);

	public void accept(Request request, Response response) throws Exception {
		logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new Object[] { request.getRemoteAddr(), request.getServiceName(),
				request.getContent() });
	}

}
