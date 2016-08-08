package com.gifisan.nio.extend.implementation;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.service.NIOFutureAcceptorService;

public class SYSTEMBeatPacketServlet extends NIOFutureAcceptorService{
	
	private Logger logger = LoggerFactory.getLogger(SYSTEMBeatPacketServlet.class);
	
	public static final String SERVICE_NAME = SYSTEMBeatPacketServlet.class.getSimpleName();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		
		logger.debug("收到心跳请求!");
		
		session.flush(future);
	}
	
}
