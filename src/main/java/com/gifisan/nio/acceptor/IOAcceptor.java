package com.gifisan.nio.acceptor;

import java.io.IOException;

import com.gifisan.nio.component.IOService;

public interface IOAcceptor extends IOService {

	public abstract void bind() throws IOException;
	
	public abstract void unbind();
}
