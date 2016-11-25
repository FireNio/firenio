package com.generallycloud.nio.component;

import java.util.Map;

//session manager event
public interface SessionMEvent {

	public abstract void fire(SocketChannelContext context, Map<Integer, SocketSession> sessions);
}
