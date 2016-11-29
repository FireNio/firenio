package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;

public interface SelectorLoopStrategy{

	public abstract void loop(SelectorLoop looper) throws IOException;
	
	public abstract void fireEvent(SelectorLoopEvent event);
	
	public abstract void stop();
	
}
