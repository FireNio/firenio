package com.gifisan.nio.front;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.concurrent.ReentrantMap;

public class FrontRouterMapping {

	private int						index		= 0;
	private List<Session>				routerList	= new ArrayList<Session>();
	private ReentrantLock				lock			= new ReentrantLock();
	//FIXME 这里不可以使用ReentrantMap
	private ReentrantMap<Integer, Session>	routerMapping	= new ReentrantMap<Integer, Session>();

	private Session getNextRouterSession() {

		ReentrantLock lock = this.lock;

		lock.lock();

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

		lock.unlock();

		return session;
	}

	public void addSession(Session session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.routerList.add(session);

		lock.unlock();
	}

	public void remove(Session session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		routerList.remove(session);

		lock.unlock();
	}

	public Session getSession(Session session) {

		Integer sessionID = session.getSessionID();

		Session _session = routerMapping.get(sessionID);

		if (_session == null) {

			return getRouterSession(session);
		}

		if (!_session.isOpened()) {

			routerMapping.remove(sessionID);

			return getRouterSession(session);
		}

		return _session;
	}

	private Session getRouterSession(Session session) {

		Integer sessionID = session.getSessionID();

		Session router_session = getNextRouterSession();

		if (router_session == null) {
			return null;
		}

		router_session.setAttribute(sessionID, session);

		routerMapping.put(sessionID, router_session);

		return router_session;
	}

	public Session getMapping(Integer sessionID) {
		return routerMapping.get(sessionID);
	}

	public void removeMapping(Integer sessionID) {
		routerMapping.remove(sessionID);
	}

}
