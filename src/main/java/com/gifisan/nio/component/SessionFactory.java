package com.gifisan.nio.component;

import java.util.Collections;
import java.util.Map;

import com.gifisan.nio.component.concurrent.ReentrantMap;

public class SessionFactory {
	
	private ReentrantMap<Integer, Session>	sessions	= new ReentrantMap<Integer, Session>();

	private final Map<Integer, Session> readOnlyManagedSessions = Collections.unmodifiableMap(sessions.getSnapshot());

	public void putSession(Session session) {

		sessions.put(session.getSessionID(), session);
	}

	public Session getSession(Integer sessionID) {

		return sessions.get(sessionID);
	}

	public void removeSession(Session session) {

		sessions.remove(session.getSessionID());
	}

	public Map<Integer, Session> getReadOnlyManagedSessions() {
		
		sessions.takeSnapshot();
		
		return readOnlyManagedSessions;
	}
}
