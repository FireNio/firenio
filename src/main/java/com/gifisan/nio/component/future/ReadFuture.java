package com.gifisan.nio.component.future;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Parameters;

public interface ReadFuture extends Future {

	public abstract Parameters getParameters();

	public abstract OutputStream getOutputStream();

	public abstract InputStream getInputStream();

	public abstract void setOutputStream(OutputStream outputStream);

	public abstract void setInputStream(InputStream inputStream);

	public abstract boolean hasOutputStream();

	public abstract boolean flushed();

	public abstract int getStreamLength();

	public abstract void write(byte b);

	public abstract void write(byte[] bytes);

	public abstract void write(byte[] bytes, int offset, int length);

	public abstract void write(String content);

	public abstract void write(String content, Charset encoding);
	
	public abstract IOEventHandle getIOEventHandle() ;

	public abstract void setIOEventHandle(IOEventHandle ioEventHandle) ;

}
