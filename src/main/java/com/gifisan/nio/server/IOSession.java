package com.gifisan.nio.server;

import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public interface IOSession extends Session {

	public abstract void flush(ReadFuture future);

	public abstract Authority getAuthority();

	public abstract LoginCenter getLoginCenter();

	/**
	 * 该方法能主动关闭EndPoint，但是可能会因为线程同步导致MainSelector抛出
	 * </BR>java.nio.channels.ClosedChannelException
	 * 
	 * @see java.nio.channels.ClosedChannelException
	 */
	public abstract void disconnect();

}
