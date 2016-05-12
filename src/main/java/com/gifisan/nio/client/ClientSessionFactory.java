package com.gifisan.nio.client;

import com.gifisan.nio.component.TCPEndPoint;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionFactory;

public class ClientSessionFactory implements SessionFactory {

	public Session getSession(TCPEndPoint endPoint, byte sessionID) {

//		return new DefaultClientSession(endPoint, sessionID);
		
		return new UnpreciseClientSession(endPoint, sessionID);
	}

}
