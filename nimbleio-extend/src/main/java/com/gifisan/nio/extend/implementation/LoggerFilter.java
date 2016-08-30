package com.gifisan.nio.extend.implementation;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.extend.service.FutureAcceptorFilter;

public class LoggerFilter extends FutureAcceptorFilter {

	private Logger	logger	= LoggerFactory.getLogger(LoggerFilter.class);

	public void accept(Session session, ReadFuture future) throws Exception {
		
		if (!future.hasOutputStream() || (future.hasOutputStream() && future.getOutputStream() != null)) {
			logger.info("请求IP：{}，服务名称：{}，请求内容：{}", new String[] { 
					session.getRemoteAddr(), 
					future.getServiceName()});
		}
	}

}
