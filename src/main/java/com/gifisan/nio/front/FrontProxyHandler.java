package com.gifisan.nio.front;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.gifisan.nio.acceptor.IOAcceptor;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class FrontProxyHandler extends IOEventHandleAdaptor {

	public static final String	RECEIVE_BROADCAST	= "RECEIVE_BROADCAST";

	private IOAcceptor			facadeAcceptor		= null;

	private Logger				logger			= LoggerFactory.getLogger(FrontProxyHandler.class);

	private RouterProxy			routerProxy		= new RouterProxy();

	public FrontProxyHandler(IOAcceptor facadeAcceptor) {
		this.facadeAcceptor = facadeAcceptor;
	}
	
	public void broadcast(ReadFuture future){
		
		Map<Integer, Session> sessions = facadeAcceptor.getReadOnlyManagedSessions();

		if (sessions == null || sessions.size() == 0) {
			return;
		}

		logger.info("广播报文：{} ", future);

		Set<Entry<Integer, Session>> entries = sessions.entrySet();

		for (Entry<Integer, Session> entry : entries) {

			Session s = entry.getValue();

			if (s.getAttribute(RECEIVE_BROADCAST) != null) {
				
				ReadFuture readFuture = ReadFutureFactory.create(s, future);
				
				readFuture.write(future.getText());

				s.flush(readFuture);
			}
		}
	}

	public void acceptAlong(Session session, ReadFuture future) throws Exception {
		
		logger.info("报文来自负载均衡：{}，报文：{}",session.getRemoteSocketAddress(),future);
		
		Integer sessionID = future.getFutureID();

		if (0 == sessionID.intValue()) {

//			broadcast(future);
			
			facadeAcceptor.broadcast(future);

			return;
		}

		Session response = (Session) session.getAttribute(sessionID);
		
		if (response != null) {
			
			if (response.closed()) {
				
				session.removeAttribute(sessionID);
				
				logger.info("回复报文到客户端失败，连接已丢失：{},{} ",session, future);
				
				return;
			}
			
			ReadFuture readFuture = ReadFutureFactory.create(response, future);
			
			readFuture.write(future.getText());
		
			response.flush(readFuture);
			
			logger.info("回复报文到客户端：{} ", future);
			
			return;
		}

		logger.info("没有该SessionID:{}", sessionID);
	}


	public RouterProxy getRouterProxy() {
		return routerProxy;
	}

	public Session getSession(Session session) {
		return routerProxy.getSession(session);
	}

	public void setFacadeAcceptor(IOAcceptor facadeAcceptor) {
		this.facadeAcceptor = facadeAcceptor;
	}

}
