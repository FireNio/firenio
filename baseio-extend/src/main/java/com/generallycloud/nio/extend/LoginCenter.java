package com.generallycloud.nio.extend;

import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.SocketSession;

public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(SocketSession session);

	public abstract void logout(SocketSession session);

	public abstract boolean isValidate(Parameters future);

	public abstract boolean login(SocketSession session, Parameters future);

}
