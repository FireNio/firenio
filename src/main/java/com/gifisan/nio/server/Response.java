package com.gifisan.nio.server;

import java.io.IOException;
import java.nio.charset.Charset;

import com.gifisan.nio.component.OutputStream;

public interface Response{
	
	/**
	 * 文本类型的response最后要做flush操作,包含数据流的response写入数据流之前进行flush操作
	 * @throws IOException
	 */
	public abstract void flush() throws IOException;
	
	public abstract void schdule();
	
	public abstract void setStream(int length) throws IOException;
	
	public abstract void write(String text);
	
	public abstract void write(String text,Charset encoding);
	
	public abstract void write(byte b) throws IOException;
	
	public abstract void write(byte[] bytes) throws IOException;

	public abstract void write(byte[] bytes, int offset, int length) throws IOException;
	
	public abstract OutputStream getOutputStream();

}
