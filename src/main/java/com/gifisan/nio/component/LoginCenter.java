package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.IOSession;


//FIXME onLogined
public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(IOSession session);

	public abstract void logout(IOSession session);

	public abstract boolean isValidate(IOSession session, ReadFuture future);

	public abstract boolean login(IOSession session, ReadFuture future);


}
