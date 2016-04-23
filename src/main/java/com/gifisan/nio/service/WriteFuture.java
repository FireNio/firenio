package com.gifisan.nio.service;

import java.io.InputStream;

public interface WriteFuture extends Future{

	public abstract InputStream getInputStream();
	
}
