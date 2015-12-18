package com.gifisan.mtp.server;

import java.io.IOException;

public interface Response extends OutputStream{
	
	public abstract void write(String content);
	
	public abstract void write(String content,String encoding);

	/**
	 * 文本类型的response最后要做flush操作
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;
	
	/**
	 * write " " to client
	 * @throws IOException
	 */
	public abstract void flushEmpty() throws IOException;
	
	public abstract void setErrorResponse() throws IOException;
	
	public abstract void setStreamResponse(int length) throws IOException;

}
