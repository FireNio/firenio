package com.gifisan.nio.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionFactory;
import com.gifisan.nio.component.TCPEndPoint;

public class ServerSessionFactory implements SessionFactory{

	public Session getSession(TCPEndPoint endPoint, byte logicSessionID) {
		
		return new ServerSession(endPoint, logicSessionID);
	}
	
}
