package com.gifisan.mtp.server;

import java.io.IOException;

public interface OutputStream {

	
	public abstract void write(byte b) throws IOException;
	
	/**
	 * 写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @throws MTPChannelException 
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes) throws IOException;

	/**
	 * 写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes, int offset, int length) throws IOException;
	
	
}
