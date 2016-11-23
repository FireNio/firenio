package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IoEventHandle;

public interface ReadFuture extends Future {
	
	public abstract IoEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IoEventHandle ioEventHandle);
	
	public abstract BaseContext getContext();
	
	public abstract boolean flushed();
	
}
