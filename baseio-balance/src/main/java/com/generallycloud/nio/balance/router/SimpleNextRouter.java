package com.generallycloud.nio.balance.router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.balance.BalanceFacadeSocketSession;
import com.generallycloud.nio.balance.BalanceReverseSocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class SimpleNextRouter extends AbstractBalanceRouter {

	private int							index			= 0;
	private ReentrantLock					lock				= new ReentrantLock();
	private List<BalanceReverseSocketSession>	routerList		= new ArrayList<>();

	private BalanceReverseSocketSession getNextRouterSession() {

		List<BalanceReverseSocketSession> list = this.routerList;

		if (list.isEmpty()) {
			return null;
		}

		BalanceReverseSocketSession session;

		if (index < list.size()) {

			session = list.get(index++);
		} else {

			index = 1;

			session = list.get(0);
		}

		return session;
	}

	public void addRouterSession(BalanceReverseSocketSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		this.routerList.add(session);

		lock.unlock();
	}

	public void removeRouterSession(BalanceReverseSocketSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		routerList.remove(session);

		lock.unlock();
	}

	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session, ReadFuture future) {

		BalanceReverseSocketSession router_session = getRouterSession(session);

		if (router_session == null) {

			return getRouterSessionFresh(session);
		}

		if (router_session.isClosed()) {

			return getRouterSessionFresh(session);
		}

		return router_session;
	}

	private BalanceReverseSocketSession getRouterSessionFresh(BalanceFacadeSocketSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			BalanceReverseSocketSession router_session = getRouterSession(session);

			if (router_session == null || router_session.isClosed()) {
				
				router_session = getNextRouterSession();
				
				if (router_session == null) {
					return null;
				}

				session.setReverseSocketSession(router_session);
			}

			return router_session;

		} finally {

			lock.unlock();
		}
	}
	
}
