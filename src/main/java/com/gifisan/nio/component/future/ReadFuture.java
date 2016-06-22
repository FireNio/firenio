package com.gifisan.nio.component.future;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.Parameters;

public interface ReadFuture extends Future{

	public abstract Parameters getParameters();
	
	public abstract OutputStream getOutputStream();
	
	public abstract InputStream getInputStream();
	
	public abstract void setOutputIOEvent(OutputStream outputStream,IOEventHandle handle);
	
	public abstract void setInputIOEvent(InputStream inputStream,IOEventHandle handle);
	
	public abstract boolean hasOutputStream();
	
	public abstract int getStreamLength();
	
	public abstract void write(byte b);

	public abstract void write(byte[] bytes);

	public abstract void write(byte[] bytes, int offset, int length);

	public abstract void write(String content);

	public abstract void write(String content, Charset encoding);
	
}
