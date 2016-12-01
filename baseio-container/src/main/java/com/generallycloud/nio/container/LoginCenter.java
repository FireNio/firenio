package com.generallycloud.nio.container;

import com.generallycloud.nio.component.SocketSession;

public interface LoginCenter extends Initializeable {

	public abstract boolean isLogined(SocketSession session);

	public abstract void logout(SocketSession session);

	public abstract boolean isValidate(String username,String password);

	public abstract boolean login(SocketSession session, String username,String password);

}
