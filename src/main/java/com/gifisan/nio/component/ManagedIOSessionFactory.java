package com.gifisan.nio.component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.server.IOSession;

public class ManagedIOSessionFactory {

	private Map<String, IOSession>	sessions	= new HashMap<String, IOSession>();
	private ReentrantLock			lock		= new ReentrantLock();

	public void putIOSession(IOSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		sessions.put(session.getSessionID(), session);

		lock.unlock();
	}

	public IOSession getIOSession(String sessionID) {
		return sessions.get(sessionID);
	}

	public void removeIOSession(IOSession session) {
		ReentrantLock lock = this.lock;

		lock.lock();

		sessions.remove(session.getSessionID());

		lock.unlock();
	}
}
