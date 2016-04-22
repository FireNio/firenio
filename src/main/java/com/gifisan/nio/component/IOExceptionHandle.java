package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.server.session.Session;

public interface IOExceptionHandle {

	public abstract void handle(Session session, IOException e);
	
}
