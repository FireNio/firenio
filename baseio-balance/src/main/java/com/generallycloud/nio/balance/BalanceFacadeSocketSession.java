package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketSession;

public interface BalanceFacadeSocketSession extends SocketSession {

	public abstract boolean isReceiveBroadcast();

	public abstract void setReceiveBroadcast(boolean receiveBroadcast);

	public abstract BalanceReverseSocketSession getReverseSocketSession();

	public abstract void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession);

}
