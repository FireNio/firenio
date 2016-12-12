package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class BalanceFacadeSocketSessionImpl extends UnsafeSocketSessionImpl implements BalanceFacadeSocketSession {

	private BalanceReverseSocketSession	reverseSocketSession	= null;

	private boolean					receiveBroadcast;

	public BalanceFacadeSocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public BalanceReverseSocketSession getReverseSocketSession() {
		return reverseSocketSession;
	}

	public void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession) {
		this.reverseSocketSession = reverseSocketSession;
	}

	public boolean isReceiveBroadcast() {
		return receiveBroadcast;
	}

	public void setReceiveBroadcast(boolean receiveBroadcast) {
		this.receiveBroadcast = receiveBroadcast;
	}
	
}
