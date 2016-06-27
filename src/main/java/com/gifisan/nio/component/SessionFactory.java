package com.gifisan.nio.component;

import java.util.Collections;
import java.util.Map;

import com.gifisan.nio.component.concurrent.ReentrantMap;

public class SessionFactory {
	
	private ReentrantMap<Long, Session>	sessions	= new ReentrantMap<Long, Session>();

	private final Map<Long, Session> readOnlyManagedSessions = Collections.unmodifiableMap(sessions.getSnapshot());

	public void putSession(Session session) {

		sessions.put(session.getSessionID(), session);
	}

	public Session getSession(Long sessionID) {

		return sessions.get(sessionID);
	}

	public void removeSession(Session session) {

		sessions.remove(session.getSessionID());
	}

	public Map<Long, Session> getReadOnlyManagedSessions() {
		return readOnlyManagedSessions;
	}
}
