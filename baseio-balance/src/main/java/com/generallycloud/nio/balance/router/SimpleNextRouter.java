package com.generallycloud.nio.balance.router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class SimpleNextRouter extends AbstractFrontRouter {

	private int			index				= 0;
	private ReentrantLock	lock					= new ReentrantLock();
	private String			SESSION_ID_ROUTER		= "_SESSION_ID_ROUTER";
	private List<SocketSession>	routerList		= new ArrayList<SocketSession>();

	private SocketSession getNextRouterSession() {

		List<SocketSession> list = this.routerList;

		if (list.isEmpty()) {
			return null;
		}

		SocketSession session;

		if (index < list.size()) {

			session = list.get(index++);
		} else {

			index = 1;

			session = list.get(0);
		}

		return session;
	}

	public void addRouterSession(SocketSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.routerList.add(session);

		lock.unlock();
	}

	public void removeRouterSession(SocketSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		routerList.remove(session);

		lock.unlock();
	}

	public SocketSession getRouterSession(SocketSession session,ReadFuture future) {

		SocketSession router_session = (SocketSession) session.getAttribute(SESSION_ID_ROUTER);

		if (router_session == null) {

			return getRouterSessionFresh(session);
		}

		if (router_session.isClosed()) {

			return getRouterSessionFresh(session);
		}

		return router_session;
	}
	
	public SocketSession getRouterSession(SocketSession session) {

		return (SocketSession) session.getAttribute(SESSION_ID_ROUTER);
	}

	private SocketSession getRouterSessionFresh(SocketSession session) {
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			
			SocketSession router_session = (SocketSession) session.getAttribute(SESSION_ID_ROUTER);
			
			if (router_session != null && router_session.isOpened()) {
				return router_session;
			}

			CloseUtil.close(router_session);
			
			router_session = getNextRouterSession();

			if (router_session == null) {
				return null;
			}

			session.setAttribute(SESSION_ID_ROUTER, router_session);

			return router_session;
			
		} finally {
			
			lock.unlock();
			
		}
	}
}
