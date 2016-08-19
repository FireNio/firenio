package com.gifisan.nio.component.protocol.nio.future;

import java.io.InputStream;

import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public interface NIOReadFuture extends ReadFuture {

	public abstract Integer getFutureID();

	public abstract String getText();

	public abstract Parameters getParameters();

	public abstract InputStream getInputStream();

	public abstract int getStreamLength();
}
