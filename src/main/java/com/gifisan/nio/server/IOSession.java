package com.gifisan.nio.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.future.ReadFuture;

public interface IOSession extends Session {

	public abstract void flush(ReadFuture future);

	/**
	 * 该方法能主动关闭EndPoint，但是可能会因为线程同步导致MainSelector抛出
	 * </BR>java.nio.channels.ClosedChannelException
	 * 
	 * @see java.nio.channels.ClosedChannelException
	 */
	public abstract void disconnect();
	
	public abstract UDPEndPoint getUDPEndPoint();
	
}
