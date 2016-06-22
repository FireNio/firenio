package com.gifisan.nio.component;

import java.io.IOException;

public interface IOAcceptor extends IOService {

	public abstract void bind() throws IOException;
	
	public abstract void unbind();
}
