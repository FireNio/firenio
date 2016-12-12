package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.UnsafeSocketSessionImpl;

public class BalanceReverseSocketSessionImpl extends UnsafeSocketSessionImpl implements BalanceReverseSocketSession{

	public BalanceReverseSocketSessionImpl(SocketChannel channel, Integer sessionID) {
		super(channel, sessionID);
	}

}
