package com.gifisan.nio.server.session;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.ReadFuture;

public interface IOSession extends Session{
	
	public abstract void schdule();

	public abstract boolean schduled();
	
	public abstract boolean flushed();
	
	public abstract void flush() throws IOException ;

	public abstract void write(byte b) throws IOException;

	public abstract void write(byte[] bytes) throws IOException;

	public abstract void write(byte[] bytes, int offset, int length) throws IOException;

	public abstract void write(String content);

	public abstract void write(String content, Charset encoding);
	
	public abstract void write(InputStream inputStream,IOExceptionHandle handle) throws IOException;

	public abstract void update(ReadFuture future);
	
}
