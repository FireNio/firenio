package com.generallycloud.nio.balance;

import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceReadFuture extends ReadFuture {

	public static int	BROADCAST	= 1;

	public static int	PUSH		= 0;

	public abstract Object getFutureID();

	public abstract void setFutureID(Object futureID);

	public abstract Integer getClientSessionID();

	public abstract void setClientSessionID(Integer sessionID);
	
	public abstract Integer getFrontSessionID();

	public abstract void setFrontSessionID(Integer sessionID);

	public abstract boolean isBroadcast();

	public abstract void setBroadcast(boolean broadcast);

	public abstract BalanceReadFuture translate();

}
