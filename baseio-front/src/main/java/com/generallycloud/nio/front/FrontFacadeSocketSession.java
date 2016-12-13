package com.generallycloud.nio.front;

import com.generallycloud.nio.component.SocketSession;

public interface FrontFacadeSocketSession extends SocketSession {

	public abstract boolean overfulfil(int size);

}
