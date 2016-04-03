package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public interface CatchWriteException {

	public void catchException(Request request, Response response, IOException exception);
	
}
