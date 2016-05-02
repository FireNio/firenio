package com.gifisan.nio.server;

import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public interface IOSession extends Session{
	
	public abstract void flush(ReadFuture future);
	
	public abstract Authority getAuthority();
	
	public abstract LoginCenter getLoginCenter();
	
}
