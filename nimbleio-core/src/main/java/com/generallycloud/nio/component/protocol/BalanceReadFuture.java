package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.IOSession;

public interface BalanceReadFuture extends ReadFuture{

	public abstract Object getFutureID();
	
	public abstract void setFutureID(Object futureID);
	
	public abstract boolean isBroadcast();
	
	public abstract boolean isReceiveBroadcast();
	
	public abstract IOWriteFuture translate(IOSession session) throws IOException;
}
