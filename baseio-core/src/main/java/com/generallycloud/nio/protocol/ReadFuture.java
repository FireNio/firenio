package com.generallycloud.nio.protocol;

import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.SocketChannelContext;

public interface ReadFuture extends Future {
	
	public abstract IoEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IoEventHandle ioEventHandle);
	
	public abstract SocketChannelContext getContext();
	
	public abstract boolean flushed();
	
	public abstract String getReadText();
	
	public abstract String getWriteText();
	
	public abstract StringBuilder getWriteTextBuffer();
	
	public abstract void write(String text);
	
	public abstract void write(char c);
	
	public abstract void write(boolean b);
	
	public abstract void write(int i);
	
	public abstract void write(long l);
	
	public abstract void write(double d);
	
}
