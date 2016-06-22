package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;


//FIXME onLogined
public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(Session session);

	public abstract void logout(Session session);

	public abstract boolean isValidate(Session session, ReadFuture future);

	public abstract boolean login(Session session, ReadFuture future);


}
