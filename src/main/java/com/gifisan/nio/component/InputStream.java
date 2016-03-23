package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface InputStream {

	public abstract int available() throws IOException;

	public abstract boolean complete();

	public abstract int read(ByteBuffer buffer) throws IOException;
	
	public abstract void completedRead(ByteBuffer buffer) throws IOException;

}
