package com.gifisan.nio.component;

import com.gifisan.nio.Looper;

public interface SelectorLoop extends SelectionAcceptor, Looper {

	public abstract boolean isMonitor(Thread thread);
}
