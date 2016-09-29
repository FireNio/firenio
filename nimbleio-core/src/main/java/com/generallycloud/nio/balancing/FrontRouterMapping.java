package com.generallycloud.nio.balancing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.IOSession;

public class FrontRouterMapping {

	private int			index			= 0;
	private List<IOSession>	routerList		= new ArrayList<IOSession>();
	private ReentrantLock	lock				= new ReentrantLock();
	private String			SESSION_ID_ROUTER	= "_SESSION_ID_ROUTER";

	private IOSession getNextRouterSession() {

		List<IOSession> list = this.routerList;

		if (list.isEmpty()) {
			return null;
		}

		IOSession session;

		if (index < list.size()) {

			session = list.get(index++);
		} else {

			index = 1;

			session = list.get(0);
		}

		return session;
	}

	public void addRouterSession(IOSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.routerList.add(session);

		lock.unlock();
	}

	public void removeRouterSession(IOSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		routerList.remove(session);

		lock.unlock();
	}

	public IOSession getRouterSession(IOSession session) {

		IOSession router_session = (IOSession) session.getAttribute(SESSION_ID_ROUTER);

		if (router_session == null) {

			return getRouterSessionFresh(session);
		}

		if (router_session.isClosed()) {

			return getRouterSessionFresh(session);
		}

		return router_session;
	}

	private IOSession getRouterSessionFresh(IOSession session) {
		
		ReentrantLock lock = this.lock;

		lock.lock();

		try {
			
			IOSession router_session = (IOSession) session.getAttribute(SESSION_ID_ROUTER);
			
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
