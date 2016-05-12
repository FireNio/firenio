package com.gifisan.nio.component;

import com.gifisan.nio.LifeCycle;

public interface SelectorLoop extends SelectionAcceptor, Runnable, LifeCycle {

	public abstract boolean isMonitor(Thread thread);
}
