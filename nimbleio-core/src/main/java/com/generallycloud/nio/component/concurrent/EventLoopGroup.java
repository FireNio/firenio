package com.generallycloud.nio.component.concurrent;

import com.generallycloud.nio.LifeCycle;

public interface EventLoopGroup extends LifeCycle{

	public abstract EventLoop getNext();
}
