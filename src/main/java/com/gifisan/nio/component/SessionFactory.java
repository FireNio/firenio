package com.gifisan.nio.component;


public interface SessionFactory {

	public abstract Session getSession(TCPEndPoint endPoint,byte sessionID);

}
