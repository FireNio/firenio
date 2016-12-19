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

	@Override
	public void flush() {
		flushed = true;
	}

	@Override
	public boolean isHeartbeat() {
		return isHeartbeat;
	}

	@Override
	public boolean isPING() {
		return isHeartbeat && isPING;
	}

	@Override
	public boolean isPONG() {
		return isHeartbeat && isPONG;
	}

	@Override
	public ChannelReadFuture setPING() {
		this.isPING = true;
		this.isHeartbeat = true;
		return this;
	}

	@Override
	public ChannelReadFuture setPONG() {
		this.isPONG = true;
		this.isHeartbeat = true;
		return this;
	}
	
	@Override
	public boolean isSilent() {
		return isSilent;
	}

	@Override
	public void setSilent(boolean isSilent) {
		this.isSilent = isSilent;
	}

	protected ByteBuf allocate(Session session,int capacity){
		return session.getByteBufAllocator().allocate(capacity);
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}
	
	
}
