package com.gifisan.nio.client;

import java.io.IOException;

public interface Connectable {

	public abstract void connect() throws IOException;
	
	public abstract void connect(boolean multi) throws IOException;
	
}
