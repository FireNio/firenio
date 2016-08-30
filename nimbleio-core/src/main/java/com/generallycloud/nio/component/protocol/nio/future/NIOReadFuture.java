package com.generallycloud.nio.component.protocol.nio.future;

import java.io.InputStream;

import com.generallycloud.nio.component.Parameters;
import com.generallycloud.nio.component.protocol.ReadFuture;

public interface NIOReadFuture extends ReadFuture {

	public abstract Integer getFutureID();

	public abstract String getText();

	public abstract Parameters getParameters();

	public abstract InputStream getInputStream();

	public abstract int getStreamLength();
}
