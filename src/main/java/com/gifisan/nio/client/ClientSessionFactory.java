package com.gifisan.nio.client;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionFactory;

public class ClientSessionFactory implements SessionFactory {

	public Session getSession(EndPoint endPoint, byte sessionID) {

		return new DefaultClientSession(endPoint, sessionID);
	}

}
