package com.gifisan.nio.servlet.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.servlet.AbstractNIOFilter;

public class LoggerFilter extends AbstractNIOFilter {
	
	private Logger logger = LoggerFactory.getLogger(LoggerFilter.class);

	public void accept(Request request, Response response) throws Exception {
		logger.info("请求IP：{},请求内容：{}",request.getRemoteAddr(),request.getContent());
	}
	
}
