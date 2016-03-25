package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface OutputStream {
	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param b
	 * @throws NIOException 
	 * @throws IOException
	 */
	public abstract int write(byte b) throws IOException;
	
	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @return 
	 * @throws NIOException 
	 * @throws IOException
	 */
	public abstract int write(byte[] bytes) throws IOException;

	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract int write(byte[] bytes, int offset, int length) throws IOException;
	
	public abstract void completedWrite(ByteBuffer buffer) throws IOException;
	
	public abstract void completedWrite(byte[] bytes, int offset, int length) throws IOException ;
	
	public abstract void completedWrite(byte [] bytes) throws IOException;
	
}
