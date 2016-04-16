package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.service.Request;

public interface CatchWriteException {

	public abstract void catchException(Request request, IOException e);
	
}
