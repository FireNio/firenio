package com.generallycloud.nio.component;

import com.generallycloud.nio.Looper;
import com.generallycloud.nio.component.DefaultEndPointWriter.EndPointWriteEvent;
import com.generallycloud.nio.component.protocol.IOWriteFuture;

public interface EndPointWriter extends Looper{
	
	public abstract void fire(EndPointWriteEvent event);

	public abstract void offer(IOWriteFuture future);
	
	public abstract void wekeupEndPoint(TCPEndPoint endPoint);

}