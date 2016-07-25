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
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.WriteFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;

public class FrontReverseAcceptorHandler extends IOEventHandleAdaptor {

	private Logger				logger			= LoggerFactory.getLogger(FrontReverseAcceptorHandler.class);
	private FrontContext		frontContext;
	private FrontRouterMapping	frontRouterMapping;

	public FrontReverseAcceptorHandler(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.frontRouterMapping = frontContext.getFrontRouterMapping();
	}

	private void broadcast(NIOReadFuture future) {

		FrontFacadeAcceptor frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();

		IOAcceptor acceptor = frontFacadeAcceptor.getAcceptor();

		Map<Integer, Session> sessions = acceptor.getReadOnlyManagedSessions();

		if (sessions == null || sessions.size() == 0) {
			return;
		}

		logger.info("广播报文：{} ", future);

		Set<Entry<Integer, Session>> entries = sessions.entrySet();

		for (Entry<Integer, Session> entry : entries) {

			Session s = entry.getValue();

			if (s.getAttribute(FrontContext.FRONT_RECEIVE_BROADCAST) != null) {

				ReadFuture readFuture = ReadFutureFactory.create(s, future);

				readFuture.write(future.getText());

				s.flush(readFuture);
			}
		}
	}

	public void acceptAlong(Session session, ReadFuture future) throws Exception {

		logger.info("报文来自负载均衡：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);
		
		NIOReadFuture f = (NIOReadFuture) future;

		Integer sessionID = f.getFutureID();

		if (0 == sessionID.intValue()) {

			broadcast(f);

			logger.info("广播报文：{}", f);

			return;
		}

		Session response = (Session) session.getAttribute(sessionID);

		if (response != null) {

			if (response.closed()) {

				session.removeAttribute(sessionID);

				logger.info("回复报文到客户端失败，连接已丢失：[ {} ],{} ", session, f);

				return;
			}

			ReadFuture readFuture = ReadFutureFactory.create(response, f);

			readFuture.write(f.getText());

			response.flush(readFuture);

			logger.info("回复报文到客户端：{} ", f);

			return;
		}

		logger.info("回复报文到客户端失败，连接已丢失，且连接已经被移除：[ {} ],{} ", session, f);
	}

	public FrontRouterMapping getRouterProxy() {
		return frontRouterMapping;
	}

	public Session getSession(Session session) {
		return frontRouterMapping.getSession(session);
	}

	public void futureSent(Session session, WriteFuture future) {
	}

}
