package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IOEventHandle;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected IOEventHandle			ioEventHandle;
	protected boolean				flushed;
	protected BaseContext			context;

	protected AbstractReadFuture(BaseContext context) {
		this.context = context;
	}

	public IOEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIOEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	public void setIOEventHandle(IOEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public boolean flushed() {
		return flushed;
	}

}
