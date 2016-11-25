package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketChannelContext;

public interface ReadFuture extends Future {
	
	public abstract IoEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IoEventHandle ioEventHandle);
	
	public abstract SocketChannelContext getContext();
	
	public abstract boolean flushed();
	
}
