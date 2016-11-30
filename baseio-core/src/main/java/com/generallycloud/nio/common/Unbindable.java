package com.generallycloud.nio.common;

import java.io.IOException;

import com.generallycloud.nio.component.concurrent.Waiter;

public interface Unbindable {

	public abstract void unbind() throws IOException;

	public abstract Waiter<IOException> asynchronousUnbind() throws IOException;
}
