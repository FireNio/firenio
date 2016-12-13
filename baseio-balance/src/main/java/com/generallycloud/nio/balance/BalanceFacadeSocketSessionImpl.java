package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class BalanceFacadeSocketSessionImpl extends UnsafeSocketSessionImpl implements BalanceFacadeSocketSession {

	private BalanceReverseSocketSession	reverseSocketSession	= null;

	public BalanceFacadeSocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public BalanceReverseSocketSession getReverseSocketSession() {
		return reverseSocketSession;
	}

	public void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession) {
		this.reverseSocketSession = reverseSocketSession;
	}
	
}
