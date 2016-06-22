package com.gifisan.nio.component;


public interface SelectorLoop extends SelectionAcceptor, Runnable {

	public abstract boolean isMonitor(Thread thread);
}
