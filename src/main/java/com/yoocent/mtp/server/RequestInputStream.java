package com.yoocent.mtp.server;

import java.io.Closeable;
import java.io.IOException;

public interface RequestInputStream extends Closeable{

	public abstract int read(byte[] bytes) throws IOException;
	
	public abstract int available() throws IOException ;
	
	public byte [] read(int length) throws IOException ;
	
}
