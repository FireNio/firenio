package com.generallycloud.nio.balancing;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.acceptor.IOAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.WriteFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;

public class FrontReverseAcceptorHandler extends IOEventHandleAdaptor {

	private Logger				logger			= LoggerFactory.getLogger(FrontReverseAcceptorHandler.class);
	private FrontContext		frontContext;
	private FrontRouterMapping	frontRouterMapping;

	public FrontReverseAcceptorHandler(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.frontRouterMapping = frontContext.getFrontRouterMapping();
	}

	private void broadcast(final NIOReadFuture future) {

		FrontFacadeAcceptor frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();

		IOAcceptor acceptor = frontFacadeAcceptor.getAcceptor();
		
		acceptor.offerSessionMEvent(new SessionMEvent() {
			
			public void handle(Map<Integer, Session> sessions) {
				
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

						try {
							s.flush(readFuture);
						} catch (IOException e) {
							logger.error(e.getMessage(),e);
						}
					}
				}
			}
		});
	}

	public void accept(Session session, ReadFuture future) throws Exception {

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

	public void futureSent(Session session, WriteFuture future) {
	}

}
