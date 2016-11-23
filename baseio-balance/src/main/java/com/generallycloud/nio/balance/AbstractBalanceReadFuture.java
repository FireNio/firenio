package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractBalanceReadFuture extends AbstractChannelReadFuture implements BalanceReadFuture {

	protected boolean	isBroadcast;

	protected Integer	sessionID;

	protected AbstractBalanceReadFuture(BaseContext context) {
		super(context);
	}

	public Integer getSessionID() {
		if (sessionID == null) {
			sessionID = 0;
		}
		return sessionID;
	}

	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	public boolean isBroadcast() {
		return isBroadcast;
	}

	public void setBroadcast(boolean isBroadcast) {
		this.isBroadcast = isBroadcast;
	}
	
}
