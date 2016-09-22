package com.generallycloud.nio.balancing;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import com.generallycloud.nio.acceptor.IOAcceptor;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.component.protocol.BalanceReadFuture;
import com.generallycloud.nio.component.protocol.IOWriteFuture;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.WriteFuture;

public class FrontReverseAcceptorHandler extends IOEventHandleAdaptor {

	private Logger				logger			= LoggerFactory.getLogger(FrontReverseAcceptorHandler.class);
	private FrontContext		frontContext;
	private FrontRouterMapping	frontRouterMapping;

	public FrontReverseAcceptorHandler(FrontContext frontContext) {
		this.frontContext = frontContext;
		this.frontRouterMapping = frontContext.getFrontRouterMapping();
	}

	private void broadcast(final BalanceReadFuture future) {

		FrontFacadeAcceptor frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();

		IOAcceptor acceptor = frontFacadeAcceptor.getAcceptor();
		
		acceptor.offerSessionMEvent(new SessionMEvent() {
			
			public void handle(Map<Integer, Session> sessions) {
				
				if (sessions == null || sessions.size() == 0) {
					return;
				}

				Iterator<Session> ss = sessions.values().iterator();
				
				if (!ss.hasNext()) {
					return;
				}
				
				IOSession _s = (IOSession) ss.next();
				
				IOWriteFuture writeFuture;
				
				try {
					writeFuture = future.translate(_s);
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
					return;
				}
				
				for (; ss.hasNext();) {

					IOSession s = (IOSession) ss.next();
					
					if (s.getAttribute(FrontContext.FRONT_RECEIVE_BROADCAST) != null) {
						
						IOWriteFuture copy = writeFuture.duplicate(s);

						try {

							s.flush(copy);

						} catch (Exception e) {

							logger.error(e.getMessage(), e);
						}
					}
				}
				
				if (_s.getAttribute(FrontContext.FRONT_RECEIVE_BROADCAST) != null) {
					try {
						_s.flush(writeFuture);
					} catch (IOException e) {
						logger.error(e.getMessage(),e);
					}
				}else {
					ReleaseUtil.release(writeFuture);
				}
			}
		});
	}

	public void accept(Session session, ReadFuture future) throws Exception {

		logger.info("报文来自负载均衡：[ {} ]，报文：{}", session.getRemoteSocketAddress(), future);
		
		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {

			broadcast(f);

			logger.info("广播报文");

			return;
		}

		Object sessionID = f.getFutureID();

		IOSession response = (IOSession) session.removeAttribute(sessionID);

		if (response != null) {

			if (response.isClosed()) {

				logger.info("回复报文到客户端失败，连接已丢失：[ {} ],{} ", session, f);

				return;
			}
			
			IOWriteFuture writeFuture = f.translate(response);
			
			response.flush(writeFuture);

			logger.info("回复报文到客户端");

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
