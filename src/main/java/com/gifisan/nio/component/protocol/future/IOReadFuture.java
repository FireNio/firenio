package com.gifisan.nio.component.protocol.future;

import java.io.IOException;

public interface IOReadFuture extends ReadFuture{

	public abstract boolean read() throws IOException;

	public abstract void flush();
	
}
