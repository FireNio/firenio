package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.gifisan.nio.component.OutputStream;

public interface Response extends OutputStream{
	
	public abstract void completedWrite(ByteBuffer buffer) throws IOException;
	
	public abstract void completedWrite(byte[] bytes, int offset, int length) throws IOException ;
	
	public abstract void completedWrite(byte [] bytes) throws IOException;
	
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
	
	public abstract void schdule();
	
	public abstract void setStreamResponse(int length) throws IOException;
	
	public abstract void write(String text);
	
	public abstract void write(String text,Charset encoding);
	
	public abstract void writeText(byte b) throws IOException;
	
	public abstract void writeText(byte[] bytes) throws IOException;

	public abstract void writeText(byte[] bytes, int offset, int length) throws IOException;

}
