package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.IOEventHandle;

public interface ReadFuture extends Future {
	
	public abstract IOEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IOEventHandle ioEventHandle);
	
	public abstract boolean flushed();
	
}
