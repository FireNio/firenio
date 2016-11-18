package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IOEventHandle;

public interface ReadFuture extends Future {
	
	public abstract IOEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IOEventHandle ioEventHandle);
	
	public abstract BaseContext getContext();
	
	public abstract boolean flushed();
	
}
