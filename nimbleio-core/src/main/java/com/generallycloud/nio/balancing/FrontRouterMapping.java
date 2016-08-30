package com.generallycloud.nio.balancing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.Session;

public class FrontRouterMapping {

	private int			index			= 0;
	private List<Session>	routerList		= new ArrayList<Session>();
	private ReentrantLock	lock				= new ReentrantLock();
	private String			SESSION_ID_ROUTER	= "_SESSION_ID_ROUTER";

	private Session getNextRouterSession() {

		List<Session> list = this.routerList;

		if (list.isEmpty()) {
			return null;
		}

		Session session;

		if (index < list.size()) {

			session = list.get(index++);
		} else {

			index = 1;

			session = list.get(0);
		}

		return session;
	}

	public void addRouterSession(Session session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.routerList.add(session);

		lock.unlock();
	}

	public void removeRouterSession(Session session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		routerList.remove(session);

		lock.unlock();
	}

	// FIXME 从SESSION获取router
	public Session getRouterSession(Session session) {

		Session router_session = (Session) session.getAttribute(SESSION_ID_ROUTER);

		if (router_session == null) {

			return getRouterSessionFresh(session);
		}

		if (!router_session.isOpened()) {

			return getRouterSessionFresh(session);
		}

		return router_session;
	}

	private Session getRouterSessionFresh(Session session) {
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			
			Session router_session = (Session) session.getAttribute(SESSION_ID_ROUTER);
			
			if (router_session != null) {
				return router_session;
			}
			
			router_session = getNextRouterSession();

			if (router_session == null) {
				return null;
			}

			Integer sessionID = session.getSessionID();
			
			router_session.setAttribute(sessionID, session);

			session.setAttribute(SESSION_ID_ROUTER, router_session);

			return router_session;
			
		} finally {
			
			lock.unlock();
			
		}
	}
}
