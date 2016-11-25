package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketChannelContext;

public abstract class AbstractReadFuture extends FutureImpl implements ReadFuture {

	protected IoEventHandle			ioEventHandle;
	protected boolean				flushed;
	protected SocketChannelContext	context;

	protected AbstractReadFuture(SocketChannelContext context) {
		this.context = context;
	}

	public IoEventHandle getIOEventHandle() {
		if (ioEventHandle == null) {
			this.ioEventHandle = context.getIoEventHandleAdaptor();
		}
		return ioEventHandle;
	}

	public void setIOEventHandle(IoEventHandle ioEventHandle) {
		this.ioEventHandle = ioEventHandle;
	}

	public boolean flushed() {
		return flushed;
	}

	public SocketChannelContext getContext() {
		return context;
	}

}
