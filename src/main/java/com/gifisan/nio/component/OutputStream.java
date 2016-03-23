package com.gifisan.nio.component;

import java.io.IOException;

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
	
	
}
