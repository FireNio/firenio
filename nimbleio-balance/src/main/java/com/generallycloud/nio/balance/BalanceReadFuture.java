package com.generallycloud.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceReadFuture extends ReadFuture{

	public abstract Object getFutureID();
	
	public abstract void setFutureID(Object futureID);
	
	public abstract boolean isBroadcast();
	
	public abstract boolean isReceiveBroadcast();
	
	public abstract IOWriteFuture translate(IOSession session) throws IOException;
}
