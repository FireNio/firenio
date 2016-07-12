package com.gifisan.nio.component.protocol.future;

import java.io.IOException;

import com.gifisan.nio.component.BufferedOutputStream;

public interface IOReadFuture extends ReadFuture{

	public abstract boolean read() throws IOException;

	public abstract void flush();
	
}
