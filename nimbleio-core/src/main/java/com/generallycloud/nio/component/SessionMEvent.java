package com.generallycloud.nio.component;

import java.util.Map;

//session manager event
public interface SessionMEvent {

	public abstract void handle(Map<Integer, Session> sessions);
}
