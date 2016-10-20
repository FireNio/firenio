package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.Session;

public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(Session session);

	public abstract void logout(Session session);

	public abstract boolean isValidate(Parameters future);

	public abstract boolean login(Session session, Parameters future);

}
