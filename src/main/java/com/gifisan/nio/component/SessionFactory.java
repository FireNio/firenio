package com.gifisan.nio.component;

import com.gifisan.nio.server.session.Session;

public interface SessionFactory {

	public abstract Session getSession(EndPoint endPoint,byte sessionID);

}
