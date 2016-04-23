package com.gifisan.nio.server.session;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionFactory;

public class ServerSessionFactory implements SessionFactory{

	public Session getSession(EndPoint endPoint, byte sessionID) {
		return new ServerSession(endPoint, sessionID);
	}
	
}
