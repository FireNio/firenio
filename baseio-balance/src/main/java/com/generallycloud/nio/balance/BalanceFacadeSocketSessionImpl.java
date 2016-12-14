package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class BalanceFacadeSocketSessionImpl extends UnsafeSocketSessionImpl implements BalanceFacadeSocketSession {

	private int msg_size;
	
	private long next_check_time;
	
	private BalanceReverseSocketSession	reverseSocketSession	= null;
	
	public BalanceFacadeSocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

	public BalanceReverseSocketSession getReverseSocketSession() {
		return reverseSocketSession;
	}

	@Override
	public boolean overfulfil(int size){
		
		long now = System.currentTimeMillis();
		
		if (now > next_check_time) {
			next_check_time = now + 1000;
			msg_size = 0;
		}
		
		return ++msg_size > size; 
	}

	public void setReverseSocketSession(BalanceReverseSocketSession reverseSocketSession) {
		this.reverseSocketSession = reverseSocketSession;
	}
	
}
