package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketSession;

public interface BalanceFacadeSocketSession extends SocketSession {

	public BalanceReverseSocketSession getReverseSocketSession();

	public void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession);

}
