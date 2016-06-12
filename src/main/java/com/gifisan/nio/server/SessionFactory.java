package com.gifisan.nio.server;

import com.gifisan.nio.concurrent.ReentrantMap;

public class SessionFactory {

	private ReentrantMap<String, IOSession>	sessions	= new ReentrantMap<String, IOSession>();

	protected void putIOSession(IOSession session) {

		sessions.put(session.getSessionID(), session);
	}

	public IOSession getIOSession(String sessionID) {

		return sessions.get(sessionID);
	}

	protected void removeIOSession(IOSession session) {

		sessions.remove(session.getSessionID());
	}

}
