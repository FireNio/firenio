package com.yoocent.mtp.server;

import java.io.IOException;

public interface Response {

	public abstract void flush() throws IOException;
	
	public abstract void write(String content);
	
	public abstract void write(String content,String encoding);

	/**
	 * 写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes);

	/**
	 * 写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes, int offset, int length);
	
	
	public abstract void setErrorResponse() throws IOException;
	
	public abstract void setStreamResponse(int length) throws IOException;

}
