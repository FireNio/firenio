package com.gifisan.mtp.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface EndPoint extends OutputStream, Closeable {

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract void write(ByteBuffer buffer) throws IOException;

}
