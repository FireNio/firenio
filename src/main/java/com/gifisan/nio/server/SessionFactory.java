package com.gifisan.nio.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class SessionFactory {

	private Map<String, IOSession>	sessions	= new HashMap<String, IOSession>();
	private ReentrantLock			lock		= new ReentrantLock();

	protected void putIOSession(IOSession session) {

		ReentrantLock lock = this.lock;

		lock.lock();

		sessions.put(session.getSessionID(), session);

		lock.unlock();
	}

	public IOSession getIOSession(String sessionID) {
		return sessions.get(sessionID);
	}

	protected void removeIOSession(IOSession session) {
		ReentrantLock lock = this.lock;

		lock.lock();

		sessions.remove(session.getSessionID());

		lock.unlock();
	}

}
