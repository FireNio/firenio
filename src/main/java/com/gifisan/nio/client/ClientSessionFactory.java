package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.server.session.Session;

public class ClientSessionFactory implements SessionFactory {

	public Session getSession(EndPoint endPoint, byte sessionID) {

		return new DefaultClientSession(endPoint, sessionID);
	}

}
