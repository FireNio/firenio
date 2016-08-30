package com.generallycloud.nio.component.protocol;

import java.io.IOException;
import java.io.InputStream;

public interface IOReadFuture extends ReadFuture{
	
	public abstract InputStream getInputStream();
	
	public abstract void setHasOutputStream(boolean hasOutputStream);

	public abstract boolean read() throws IOException;

	public abstract void flush();
	
	public abstract boolean isBeatPacket();
	
}
