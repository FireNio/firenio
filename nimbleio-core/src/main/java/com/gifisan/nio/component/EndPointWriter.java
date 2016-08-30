package com.gifisan.nio.component;

import com.gifisan.nio.Looper;
import com.gifisan.nio.component.DefaultEndPointWriter.EndPointWriteEvent;
import com.gifisan.nio.component.protocol.IOWriteFuture;

public interface EndPointWriter extends Looper{
	
	public abstract void fire(EndPointWriteEvent event);

	public abstract void offer(IOWriteFuture future);
	
	public abstract void wekeupEndPoint(TCPEndPoint endPoint);

}