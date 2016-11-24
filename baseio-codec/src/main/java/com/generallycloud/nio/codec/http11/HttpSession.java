package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public interface HttpSession {

	public abstract void active(SocketSession ioSession);

	public abstract void flush(ReadFuture future) throws IOException;

	public abstract long getCreateTime();

	public abstract SocketSession getIoSession();

	public abstract long getLastAccessTime();

	public abstract String getSessionID();
	
	public abstract boolean isValidate();
	
	public abstract HttpContext getContext();

}