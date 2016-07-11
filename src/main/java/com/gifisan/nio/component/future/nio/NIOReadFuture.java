package com.gifisan.nio.component.future.nio;

import java.io.InputStream;
import java.io.OutputStream;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ReadFuture;

public interface NIOReadFuture extends ReadFuture {

	public abstract Integer getFutureID();

	public abstract String getServiceName();

	public abstract String getText();

	public abstract Parameters getParameters();

	public abstract OutputStream getOutputStream();

	public abstract InputStream getInputStream();

	public abstract void setOutputStream(OutputStream outputStream);

	public abstract void setInputStream(InputStream inputStream);

	public abstract boolean hasOutputStream();

	public abstract int getStreamLength();
}
