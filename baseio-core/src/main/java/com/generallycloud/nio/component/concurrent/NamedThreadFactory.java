package com.generallycloud.nio.component.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private ThreadGroup			group;
	private final AtomicInteger	threadNumber	= new AtomicInteger(1);
	private final String		namePrefix;

	public NamedThreadFactory(String namePrefix) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.namePrefix = namePrefix;
	}

	public Thread newThread(Runnable r) {
		Thread t = new PooledThread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon())
			t.setDaemon(false);
		if (t.getPriority() != Thread.NORM_PRIORITY)
			t.setPriority(Thread.NORM_PRIORITY);
		return t;
	}

	public boolean inFactory(Thread thread) {
		return thread instanceof PooledThread;
	}

	class PooledThread extends Thread {

		public PooledThread(ThreadGroup group, Runnable r, String string, int i) {
			super(group, r, string, i);
		}
	}

}
