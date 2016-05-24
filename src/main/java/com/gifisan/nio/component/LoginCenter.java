package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;


//FIXME onLogined
public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(IOSession session);

	public abstract void logout(IOSession session);

	public abstract boolean isValidate(IOSession session, ServerReadFuture future);

	public abstract boolean login(IOSession session, ServerReadFuture future);


}
