package com.generallycloud.nio.component.protocol.http11;

import java.io.IOException;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;

public interface HttpSession {

	public abstract void active(Session ioSession);

	public abstract void flush(ReadFuture future) throws IOException;

	public abstract long getCreateTime();

	public abstract Session getIOSession();

	public abstract long getLastAccessTime();

	public abstract String getSessionID();
	
	public abstract boolean isValidate();
	
	public abstract HttpContext getContext();

}