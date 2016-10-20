package com.generallycloud.nio.acceptor;

import java.util.concurrent.locks.ReentrantLock;

public class CoreProcessors {
	
	protected CoreProcessors(int coreSize) {
		this.coreSize = coreSize;
	}

	private int			coreSize;

	private int			currentCoreIndex;

	private ReentrantLock	reentrantLock	= new ReentrantLock();

	public int getCoreSize() {
		return coreSize;
	}

	public int getCurrentCoreIndex() {
		return currentCoreIndex;
	}

	public void setCurrentCoreIndex(int currentCoreIndex) {
		this.currentCoreIndex = currentCoreIndex;
	}

	public ReentrantLock getReentrantLock() {
		return reentrantLock;
	}
}
