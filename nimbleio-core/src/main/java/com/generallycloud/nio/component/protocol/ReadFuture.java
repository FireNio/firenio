package com.generallycloud.nio.component.protocol;

import java.nio.charset.Charset;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.IOEventHandle;

public interface ReadFuture extends Future {
	
	public abstract IOEventHandle getIOEventHandle() ;

	public abstract BufferedOutputStream getWriteBuffer();
	
	public abstract void setIOEventHandle(IOEventHandle ioEventHandle) ;
	
	public abstract void write(byte b);

	public abstract void write(byte[] bytes);
	
	public abstract void write(byte[] bytes, int offset, int length);
	
	public abstract void write(String content);
	
	public abstract void write(String content, Charset encoding);

	public abstract boolean flushed();
}
