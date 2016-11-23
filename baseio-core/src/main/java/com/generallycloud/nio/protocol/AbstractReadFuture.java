package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IoEventHandle;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected IoEventHandle		ioEventHandle;
	protected boolean			flushed;
	protected BaseContext		context;

	protected AbstractReadFuture(BaseContext context) {
		this.context = context;
	}

	public IoEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIOEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	public void setIOEventHandle(IoEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public boolean flushed() {
		return flushed;
	}

	public BaseContext getContext() {
		return context;
	}

}
