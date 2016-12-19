package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;

public abstract class SelectorLoopEventAdapter implements SelectorLoopEvent{

	@Override
	public void close() throws IOException {
	}

	@Override
	public boolean isPositive() {
		return true;
	}
	
}
