package com.gifisan.nio.component;

import java.io.IOException;

public interface IOExceptionHandle {

	public abstract void handle(Session session,Future future, IOException e);
	
}
