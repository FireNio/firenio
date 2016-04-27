package com.gifisan.nio.server.session;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public interface IOSession extends Session{
	
	public abstract void flush(ReadFuture future) throws IOException ;

}
