package com.gifisan.nio.component.future;

import java.nio.charset.Charset;

public interface ServerReadFuture extends ReadFuture {

	public abstract void write(byte b);

	public abstract void write(byte[] bytes);

	public abstract void write(byte[] bytes, int offset, int length);

	public abstract void write(String content);

	public abstract void write(String content, Charset encoding);

}
