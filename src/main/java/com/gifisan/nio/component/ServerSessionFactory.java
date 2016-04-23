package com.gifisan.nio.component;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.server.session.Session;

public class ServerSessionFactory implements SessionFactory{

	public Session getSession(EndPoint endPoint, byte sessionID) {
		return new ServerSession(endPoint, sessionID);
	}
	
}
