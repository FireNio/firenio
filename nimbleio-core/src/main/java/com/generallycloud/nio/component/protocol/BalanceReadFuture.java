package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.IOSession;

public interface BalanceReadFuture extends ReadFuture{

	public abstract Integer getFutureID();
	
	public abstract void setFutureID(Integer futureID);
	
	public abstract IOWriteFuture translate(IOSession session) throws IOException;
}
