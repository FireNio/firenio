package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public interface CatchWriteException {

	public void catchException(Request request, Response response, IOException exception);
	
}
