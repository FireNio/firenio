package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.component.future.Future;

public interface IOExceptionHandle {

	public abstract void handle(Session session,Future future, IOException e);
	
}
