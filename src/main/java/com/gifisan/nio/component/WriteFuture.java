package com.gifisan.nio.component;

import java.io.InputStream;

public interface WriteFuture extends Future{

	public abstract InputStream getInputStream();
	
}
