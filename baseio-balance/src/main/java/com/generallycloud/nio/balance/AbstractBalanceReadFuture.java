package com.generallycloud.nio.balance;

import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.protocol.AbstractChannelReadFuture;

public abstract class AbstractBalanceReadFuture extends AbstractChannelReadFuture implements BalanceReadFuture {

	protected boolean	isBroadcast;

//	protected Integer	clientSessionID;
//	
//	protected Integer	frontSessionID;
	
	protected Integer sessionID = 0;

	protected AbstractBalanceReadFuture(SocketChannelContext context) {
		super(context);
	}

//	@Override
//	public Integer getClientSessionID() {
//		if (clientSessionID == null) {
//			clientSessionID = 0;
//		}
//		return clientSessionID;
//	}
//
//	@Override
//	public void setClientSessionID(Integer sessionID) {
//		this.clientSessionID = sessionID;
//	}
//
//	@Override
//	public Integer getFrontSessionID() {
//		if (frontSessionID == null) {
//			frontSessionID = 0;
//		}
//		return frontSessionID;
//	}
//
//	@Override
//	public void setFrontSessionID(Integer sessionID) {
//		this.frontSessionID = sessionID;
//	}
	
	@Override
	public boolean isBroadcast() {
		return isBroadcast;
	}
	
	@Override
	public Integer getSessionID() {
		return sessionID;
	}

	@Override
	public void setSessionID(Integer sessionID) {
		this.sessionID = sessionID;
	}

	@Override
	public void setBroadcast(boolean isBroadcast) {
		this.isBroadcast = isBroadcast;
	}
	
}
