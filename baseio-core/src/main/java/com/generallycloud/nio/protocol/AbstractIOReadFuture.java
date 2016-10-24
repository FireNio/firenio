package com.generallycloud.nio.protocol;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.BaseContext;


public abstract class AbstractIOReadFuture extends AbstractReadFuture implements IOReadFuture {

	protected AbstractIOReadFuture(BaseContext context) {
		super(context);
	}

	protected boolean	isHeartbeat;

	protected boolean	isPING;

	protected boolean	isPONG;

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

	public IOReadFuture setPING() {
		this.isPING = true;
		this.isHeartbeat = true;
		return this;
	}

	public IOReadFuture setPONG() {
		this.isPONG = true;
		this.isHeartbeat = true;
		return this;
	}
	
	protected ByteBuf allocate(int capacity){
		return context.getHeapByteBufferPool().allocate(capacity);
	}
}
