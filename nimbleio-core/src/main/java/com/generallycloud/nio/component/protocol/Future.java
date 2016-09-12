package com.generallycloud.nio.component.protocol;

import com.generallycloud.nio.Releasable;

public interface Future extends Releasable{

	public abstract void attach(Object attachment);

	public abstract Object attachment();
}
