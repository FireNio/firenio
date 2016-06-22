package com.gifisan.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;

public interface IOAcceptor extends IOService {

	public abstract void bind(InetSocketAddress socketAddress) throws IOException;
	
	public abstract void unbind();
}
