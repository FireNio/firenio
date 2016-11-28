package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;

public abstract class SelectorLoopEventAdapter implements SelectorLoopEvent{

	public void close() throws IOException {
	}

	public boolean isPositive() {
		return true;
	}
	
}
