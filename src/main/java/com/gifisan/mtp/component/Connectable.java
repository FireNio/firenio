package com.gifisan.mtp.component;

import java.io.IOException;

public interface Connectable {

	public abstract void connect() throws IOException;
	
	public abstract void connect(boolean unique) throws IOException;
	
}
