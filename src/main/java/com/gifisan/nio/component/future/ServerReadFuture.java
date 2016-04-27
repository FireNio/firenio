package com.gifisan.nio.component.future;

import java.io.IOException;
import java.nio.charset.Charset;

public interface ServerReadFuture extends ReadFuture{
	
	public abstract void write(byte b) throws IOException;

	public abstract void write(byte[] bytes) throws IOException;

	public abstract void write(byte[] bytes, int offset, int length) throws IOException;

	public abstract void write(String content);

	public abstract void write(String content, Charset encoding);
	
}
