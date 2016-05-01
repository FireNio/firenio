package com.gifisan.nio.component;

import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public interface LoginCenter extends LifeCycle {

	public abstract Authority login(IOSession session, ServerReadFuture future);

	public abstract boolean logined(IOSession session, ServerReadFuture future);

	public abstract void logout(IOSession session, ServerReadFuture future);

	public abstract boolean validate(IOSession session, ServerReadFuture future);	
}
