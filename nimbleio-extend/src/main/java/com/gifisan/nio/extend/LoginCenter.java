package com.gifisan.nio.extend;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;


//FIXME onLogined
public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(Session session);

	public abstract void logout(Session session);

	public abstract boolean isValidate(Parameters future);

	public abstract boolean login(Session session, Parameters future);


}
