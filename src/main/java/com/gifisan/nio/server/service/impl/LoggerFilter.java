package com.gifisan.nio.server.service.impl;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.service.AbstractNIOFilter;

public class LoggerFilter extends AbstractNIOFilter {

	private Logger	logger	= LoggerFactory.getLogger(LoggerFilter.class);

	public void accept(IOSession session,ServerReadFuture future) throws Exception {
		
		if (future.hasOutputStream() && future.getOutputStream() != null) {
			logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { 
					session.getRemoteAddr(), 
					future.getServiceName(),
					future.getText() });
		}
	}

}
