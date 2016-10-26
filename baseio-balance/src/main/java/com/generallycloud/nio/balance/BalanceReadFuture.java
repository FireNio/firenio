package com.generallycloud.nio.balance;

import java.io.IOException;

import com.generallycloud.nio.protocol.IOWriteFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceReadFuture extends ReadFuture {

	public static int	BROADCAST	= 1;

	public static int	PUSH		= 0;

	public abstract Object getFutureID();

	public abstract void setFutureID(Object futureID);

	public abstract Integer getSessionID();

	public abstract void setSessionID(Integer sessionID);

	public abstract boolean isBroadcast();

	public abstract void setBroadcast(boolean broadcast);

	public abstract boolean isReceiveBroadcast();

	public abstract IOWriteFuture translate() throws IOException;

}
