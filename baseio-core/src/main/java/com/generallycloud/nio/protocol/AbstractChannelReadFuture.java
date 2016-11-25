package com.generallycloud.nio.protocol;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SocketChannelContext;


public abstract class AbstractChannelReadFuture extends AbstractReadFuture implements ChannelReadFuture {

	protected AbstractChannelReadFuture(SocketChannelContext context) {
		super(context);
	}

	protected boolean	isHeartbeat;

	protected boolean	isPING;

	protected boolean	isPONG;
	
	protected boolean isSilent;

	public void flush() {
		flushed = true;
	}

	public boolean isHeartbeat() {
		return isHeartbeat;
	}

	public boolean isPING() {
		return isHeartbeat && isPING;
	}

	public boolean isPONG() {
		return isHeartbeat && isPONG;
	}

	public ChannelReadFuture setPING() {
		this.isPING = true;
		this.isHeartbeat = true;
		return this;
	}

	public ChannelReadFuture setPONG() {
		this.isPONG = true;
		this.isHeartbeat = true;
		return this;
	}
	
	public boolean isSilent() {
		return isSilent;
	}

	public void setSilent(boolean isSilent) {
		this.isSilent = isSilent;
	}

	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}

	public SocketChannelContext getContext() {
		return context;
	}
	
	
}
