package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionFactory;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public class FixedSessionFactory extends SessionFactory {

	protected FixedSessionFactory(NIOContext context) {
		super(context);
	}

	private ReentrantMap<String, Session>	sessions	= new ReentrantMap<String, Session>();

	public void putSession(String username, Session session) {
		sessions.put(username, session);
	}

	public Session getSession(String username) {

		return sessions.get(username);
	}

	public void removeSession(String username) {

		sessions.remove(username);
	}

}
