package com.generallycloud.nio.component;

import java.util.Map;

//session manager event
public interface SessionMEvent {

	public abstract void fire(BaseContext context, Map<Integer, Session> sessions);
}
