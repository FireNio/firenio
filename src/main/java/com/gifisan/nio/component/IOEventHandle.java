package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.component.future.Future;

public interface IOEventHandle {

	public abstract void handle(Session session,Future future, IOException e);
	
	public abstract void handle(Session session,Future future);
	
}
